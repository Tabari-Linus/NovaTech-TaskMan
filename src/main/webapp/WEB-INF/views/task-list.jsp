<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>

<jsp:include page="/WEB-INF/views/header.jsp"/>

<h2>My Tasks</h2>

<div class="card mb-4">
    <div class="card-header">
        <h5>Filter Tasks</h5>
    </div>
    <div class="card-body">
        <form action="${pageContext.request.contextPath}/tasks" method="get" class="row g-3">
            <!-- Status Filter -->
            <div class="col-md-2">
                <label for="status" class="form-label">Status</label>
                <select class="form-control" id="status" name="status">
                    <option value="">All Statuses</option>
                    <option value="PENDING" ${statusFilter eq 'PENDING' ? 'selected' : ''}>Pending</option>
                    <option value="IN_PROGRESS" ${statusFilter eq 'IN_PROGRESS' ? 'selected' : ''}>In Progress</option>
                    <option value="COMPLETED" ${statusFilter eq 'COMPLETED' ? 'selected' : ''}>Completed</option>
                </select>
            </div>


            <div class="col-md-3">
                <label for="startDate" class="form-label">From Date</label>
                <input type="date" class="form-control" id="startDate" name="startDate" value="${startDate}">
            </div>
            <div class="col-md-3">
                <label for="endDate" class="form-label">To Date</label>
                <input type="date" class="form-control" id="endDate" name="endDate" value="${endDate}">
            </div>


            <div class="col-md-2">
                <label for="sortOrder" class="form-label">Sort Order</label>
                <select class="form-control" id="sortOrder" name="sortOrder">
                    <option value="asc" ${sortOrder ne 'desc' ? 'selected' : ''}>Oldest First</option>
                    <option value="desc" ${sortOrder eq 'desc' ? 'selected' : ''}>Newest First</option>
                </select>
            </div>

            <div class="col-md-2 d-flex align-items-end">
                <button type="submit" class="btn btn-primary me-2">Apply </button>
                <a href="${pageContext.request.contextPath}/tasks" class="btn btn-outline-secondary">Clear</a>
            </div>
        </form>

        <c:if test="${not empty dateError}">
            <div class="alert alert-danger mt-3" role="alert">
                ${dateError}
            </div>
        </c:if>
    </div>
</div>

<div class="row mb-3">
    <div class="col-md-6">
        <div class="btn-group" role="group">
            <a href="${pageContext.request.contextPath}/tasks" class="btn ${empty statusFilter ? 'btn-primary' : 'btn-outline-primary'}">All</a>
            <a href="${pageContext.request.contextPath}/tasks?status=PENDING" class="btn ${statusFilter eq 'PENDING' ? 'btn-primary' : 'btn-outline-primary'}">Pending</a>
            <a href="${pageContext.request.contextPath}/tasks?status=IN_PROGRESS" class="btn ${statusFilter eq 'IN_PROGRESS' ? 'btn-primary' : 'btn-outline-primary'}">In Progress</a>
            <a href="${pageContext.request.contextPath}/tasks?status=COMPLETED" class="btn ${statusFilter eq 'COMPLETED' ? 'btn-primary' : 'btn-outline-primary'}">Completed</a>
        </div>
    </div>
    <div class="col-md-6 text-right">
        <a href="${pageContext.request.contextPath}/tasks/new" class="btn btn-success">Add New Task</a>
    </div>
</div>

<c:if test="${not empty startDate or not empty endDate or sortOrder eq 'desc'}">
    <div class="alert alert-info mb-3">
        <c:if test="${not empty startDate or not empty endDate}">
            <strong>Date Filter:</strong>
            <c:if test="${not empty startDate}">From ${startDate}</c:if>
            <c:if test="${not empty startDate and not empty endDate}"> - </c:if>
            <c:if test="${not empty endDate}">To ${endDate}</c:if>
        </c:if>

        <c:if test="${sortOrder eq 'desc'}">
            <strong><c:if test="${not empty startDate or not empty endDate}"> | </c:if>Sort Order:</strong> Newest First
        </c:if>

        <a href="${pageContext.request.contextPath}/tasks${not empty statusFilter ? '?status='.concat(statusFilter) : ''}" class="float-right">Clear Filters</a>
    </div>
</c:if>

<c:choose>
    <c:when test="${empty tasks}">
        <div class="alert alert-info">
            No tasks found matching your criteria. Click the "Add New Task" button to create one.
        </div>
    </c:when>
    <c:otherwise>
        <div class="table-responsive">
            <table class="table table-striped table-hover">
                <thead class="thead-dark">
                    <tr>
                        <th>Title</th>
                        <th>
                            Due Date
                            <c:if test="${sortOrder eq 'desc'}">
                                <i class="fas fa-sort-down ml-1"></i>
                            </c:if>
                            <c:if test="${sortOrder ne 'desc' and not empty sortOrder}">
                                <i class="fas fa-sort-up ml-1"></i>
                            </c:if>
                        </th>
                        <th>Status</th>
                        <th>Priority</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="task" items="${tasks}">
                        <tr>
                            <td>${task.title}</td>
                            <td>
                                <c:choose>
                                    <c:when test="${task.dueDate != null}">
                                        <fmt:formatDate value="${task.dueDate}" pattern="MMM dd, yyyy" />
                                    </c:when>
                                    <c:otherwise>
                                        <span class="text-muted">No due date</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${task.status eq 'PENDING'}">
                                        <span class="badge badge-warning px-3 py-1">Pending</span>
                                    </c:when>
                                    <c:when test="${task.status eq 'IN_PROGRESS'}">
                                        <span class="badge badge-info px-3 py-1">In Progress</span>
                                    </c:when>
                                    <c:when test="${task.status eq 'COMPLETED'}">
                                        <span class="badge badge-success px-3 py-1">Completed</span>
                                    </c:when>
                                </c:choose>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${task.priority eq 'HIGH'}">
                                        <span class="badge badge-danger  px-3 py-1">High</span>
                                    </c:when>
                                    <c:when test="${task.priority eq 'MEDIUM'}">
                                        <span class="badge badge-warning px-3 py-1">Medium</span>
                                    </c:when>
                                    <c:when test="${task.priority eq 'LOW'}">
                                        <span class="badge badge-secondary px-3 py-1">Low</span>
                                    </c:when>
                                </c:choose>
                            </td>
                            <td>
                                <div class="btn-group" role="group">
                                    <a href="${pageContext.request.contextPath}/tasks/edit/${task.id}" class="btn btn-sm btn-primary ">Edit</a>
                                    <a href="${pageContext.request.contextPath}/tasks/delete/${task.id}" class="btn btn-sm btn-danger"
                                       onclick="return confirm('Are you sure you want to delete this task?')">Delete</a>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </c:otherwise>
</c:choose>

<jsp:include page="/WEB-INF/views/footer.jsp"/>