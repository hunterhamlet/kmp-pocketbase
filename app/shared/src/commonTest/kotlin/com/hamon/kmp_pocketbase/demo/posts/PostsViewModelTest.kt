package com.hamon.kmp_pocketbase.demo.posts

import com.hamon.kmp_pocketbase.generated.PostsRecord
import com.hamon.kmp_pocketbase.network.pocketbase.PocketBaseResult
import com.hamon.kmp_pocketbase.network.pocketbase.dto.RecordModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class PostsViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun post(
        id: String = "1",
        title: String = "T",
        content: String = "C",
    ) = RecordModel(id = id, fields = PostsRecord(id = id, title = title, content = content, author = ""))

    private fun viewModel(repo: PostsRepository = FakePostsRepository()) = PostsViewModel(repo)

    @Test
    fun initialStateLoadsPostsSuccessfully() =
        runTest {
            val vm = viewModel(FakePostsRepository(posts = listOf(post())))
            assertIs<PostsUiState.Success>(vm.postsState)
        }

    @Test
    fun loadPostsFailureTransitionsToError() =
        runTest {
            val vm = viewModel(FakePostsRepository(getResult = PocketBaseResult.Failure(RuntimeException("error"))))
            assertIs<PostsUiState.Error>(vm.postsState)
        }

    @Test
    fun createPostSuccessUpdatesActionStateToDone() =
        runTest {
            val vm = viewModel()
            vm.createPost("Title", "Content")
            assertIs<PostActionState.Done>(vm.actionState)
        }

    @Test
    fun createPostFailureTransitionsToError() =
        runTest {
            val vm = viewModel(FakePostsRepository(mutateResult = PocketBaseResult.Failure(RuntimeException("err"))))
            vm.createPost("Title", "Content")
            assertIs<PostActionState.Error>(vm.actionState)
        }

    @Test
    fun updatePostSuccessUpdatesActionStateToDone() =
        runTest {
            val vm = viewModel()
            vm.updatePost("1", "New Title", "New Content")
            assertIs<PostActionState.Done>(vm.actionState)
        }

    @Test
    fun deletePostSuccessUpdatesActionStateToDone() =
        runTest {
            val vm = viewModel()
            vm.deletePost("1")
            assertIs<PostActionState.Done>(vm.actionState)
        }

    @Test
    fun deletePostFailureTransitionsToError() =
        runTest {
            val vm = viewModel(FakePostsRepository(deleteResult = PocketBaseResult.Failure(RuntimeException("err"))))
            vm.deletePost("1")
            assertIs<PostActionState.Error>(vm.actionState)
        }

    @Test
    fun resetActionStateSetsStateToIdle() =
        runTest {
            val vm = viewModel()
            vm.createPost("T", "C")
            vm.resetActionState()
            assertIs<PostActionState.Idle>(vm.actionState)
        }
}

private class FakePostsRepository(
    posts: List<RecordModel<PostsRecord>> = emptyList(),
    private val getResult: PocketBaseResult<List<RecordModel<PostsRecord>>> = PocketBaseResult.Success(posts),
    private val mutateResult: PocketBaseResult<RecordModel<PostsRecord>> =
        PocketBaseResult.Success(
            RecordModel(fields = PostsRecord(id = "", title = "", content = "", author = "")),
        ),
    private val deleteResult: PocketBaseResult<Unit> = PocketBaseResult.Success(Unit),
) : PostsRepository {
    override suspend fun getPosts() = getResult

    override suspend fun createPost(
        title: String,
        content: String,
    ) = mutateResult

    override suspend fun updatePost(
        id: String,
        title: String,
        content: String,
    ) = mutateResult

    override suspend fun deletePost(id: String) = deleteResult
}
