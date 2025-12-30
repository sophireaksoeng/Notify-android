package com.team.notify.taskflow.presentation.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team.notify.taskflow.data.dao.SpaceMemberDao
import com.team.notify.taskflow.data.entities.SpaceMemberEntity
import com.team.notify.taskflow.data.entities.TaskEntity
import com.team.notify.taskflow.data.repository.interfaces.TaskRepository
import com.team.notify.taskflow.model.TaskStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class TaskFilters(
    val query: String = "",
    val status: Set<TaskStatus> = emptySet(),
    val assigneeId: String? = null,
    val label: String? = null
)

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repo: TaskRepository,
    private val memberDao: SpaceMemberDao
) : ViewModel() {

    private val _spaceId = MutableStateFlow("default-space")
    val spaceId: StateFlow<String> = _spaceId.asStateFlow()

    private val _selectedTaskId = MutableStateFlow<String?>(null)
    val selectedTaskId: StateFlow<String?> = _selectedTaskId.asStateFlow()

    private val _filters = MutableStateFlow(TaskFilters())
    val filters: StateFlow<TaskFilters> = _filters.asStateFlow()

    val members: StateFlow<List<SpaceMemberEntity>> =
        spaceId
            .flatMapLatest { sid -> memberDao.membersForSpace(sid) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val tasks: StateFlow<List<TaskEntity>> =
        combine(spaceId, filters) { sid, f -> sid to f }
            .flatMapLatest { (sid, f) ->
                repo.tasks(sid, f.query)
                    .map { list -> applyFilters(list, f) }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val selectedTask: StateFlow<TaskEntity?> =
        selectedTaskId
            .flatMapLatest { id -> if (id == null) flowOf(null) else repo.task(id) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun onEnter(spaceId: String) {
        _spaceId.value = spaceId
    }

    private fun applyFilters(list: List<TaskEntity>, f: TaskFilters): List<TaskEntity> {
        return list.asSequence()
            .filter { t -> f.status.isEmpty() || f.status.contains(t.status) }
            .filter { t -> f.assigneeId == null || t.assigneeId == f.assigneeId }
            .filter { t -> f.label == null || t.labels.any { it.equals(f.label, ignoreCase = true) } }
            .toList()
    }

    fun selectTask(id: String?) {
        _selectedTaskId.value = id
    }

    fun setQuery(q: String) {
        _filters.value = _filters.value.copy(query = q)
    }

    fun toggleStatusFilter(status: TaskStatus) {
        val current = _filters.value.status.toMutableSet()
        if (current.contains(status)) current.remove(status) else current.add(status)
        _filters.value = _filters.value.copy(status = current)
    }

    fun setAssigneeFilter(userId: String?) {
        _filters.value = _filters.value.copy(assigneeId = userId)
    }

    fun setLabelFilter(label: String?) {
        _filters.value = _filters.value.copy(label = label)
    }

    fun clearFilters() {
        _filters.value = TaskFilters(query = _filters.value.query)
    }

    fun createTask() = createTask(pageId = null)

    fun createTask(pageId: String?) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val t = TaskEntity(
                id = UUID.randomUUID().toString(),
                spaceId = _spaceId.value,
                pageId = pageId,
                title = "New task",
                description = "",
                status = TaskStatus.TODO,
                deadline = null,
                isCompleted = false,
                assigneeId = null,
                labels = emptyList(),
                updatedAt = now
            )
            repo.insert(t)
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch { repo.deleteById(taskId) }
    }

    fun updateTitle(taskId: String, title: String) {
        viewModelScope.launch {
            val old = repo.getTaskById(taskId).firstOrNull() ?: return@launch
            repo.insert(old.copy(title = title, updatedAt = System.currentTimeMillis()))
        }
    }

    fun updateDescription(taskId: String, desc: String) {
        viewModelScope.launch {
            val old = repo.getTaskById(taskId).firstOrNull() ?: return@launch
            repo.insert(old.copy(description = desc, updatedAt = System.currentTimeMillis()))
        }
    }

    fun setStatus(taskId: String, status: TaskStatus) {
        viewModelScope.launch {
            val old = repo.getTaskById(taskId).firstOrNull() ?: return@launch
            repo.insert(
                old.copy(
                    status = status,
                    isCompleted = (status == TaskStatus.DONE),
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun toggleCompleted(taskId: String) {
        viewModelScope.launch {
            val old = repo.getTaskById(taskId).firstOrNull() ?: return@launch
            val newCompleted = !old.isCompleted
            val newStatus = if (newCompleted) TaskStatus.DONE else TaskStatus.TODO
            repo.insert(old.copy(isCompleted = newCompleted, status = newStatus, updatedAt = System.currentTimeMillis()))
        }
    }

    fun updateAssignee(taskId: String, userId: String?) {
        viewModelScope.launch {
            val old = repo.getTaskById(taskId).firstOrNull() ?: return@launch
            repo.insert(old.copy(assigneeId = userId, updatedAt = System.currentTimeMillis()))
        }
    }

    fun updateDeadline(taskId: String, deadlineMillis: Long?) {
        viewModelScope.launch {
            val old = repo.getTaskById(taskId).firstOrNull() ?: return@launch
            repo.insert(old.copy(deadline = deadlineMillis, updatedAt = System.currentTimeMillis()))
        }
    }
}
