# Team Task & Project Management API

A production-grade **Modular Monolith RESTful API** built with **Java 21** and **Spring Boot 3.3**, designed for collaborative team workspace, project, and task management (similar to Trello/Jira core backend).

## Key Architectural Highlights
- **Modular Monolith (Package-by-Feature):** Cleanly separated domain modules (`auth`, `user`, `workspace`, `project`, `task`, `notification`, `security`, `common`) with strict boundary enforcement.
- **Two-Tier Security & Access Control:**
    - **System-Level RBAC:** Stateless JWT Authentication (`Spring Security 6` + `JJWT 0.12.x`) with `ROLE_USER` and `ROLE_ADMIN`.
    - **Workspace-Level RBAC:** Dedicated `WorkspaceAccessService` enforcing fine-grained permissions (`OWNER`, `ADMIN`, `MEMBER`) via composite keys (`@EmbeddedId` & `@MapsId`).
- **Rich Domain Model & State Machine:** Encapsulated business rules within entities, including strict task status transition validation (`TODO` -> `IN_PROGRESS` -> `DONE`).
- **Event-Driven Decoupling:** Asynchronous, post-commit notification handling using `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` and `@Async` upon task assignment (`TaskAssignedEvent`).
- **Dynamic Filtering & Pagination:** Type-safe dynamic queries using Spring Data JPA `Specification<Task>` (`JpaSpecificationExecutor`) combined with pagination and sorting.
- **JPA / Hibernate Optimization:** Explicit `FetchType.LAZY` on all `@ManyToOne` associations to prevent N+1 query issues, custom database indexing (`@Index`), and automated JPA Auditing (`@CreatedDate`, `@LastModifiedDate`).
- **API Documentation & Error Standardization:** Interactive Swagger UI (`Springdoc OpenAPI 3`) with pre-configured Bearer JWT authentication and centralized exception handling (`@RestControllerAdvice`).

## Technology Stack
- **Language & Framework:** Java 21, Spring Boot 3.3.5
- **Persistence:** Spring Data JPA, Hibernate, PostgreSQL
- **Security:** Spring Security 6, JSON Web Tokens (JJWT 0.12.6), BCrypt
- **Validation & Utilities:** Jakarta Bean Validation (JSR-380), Project Lombok
- **Documentation:** Springdoc OpenAPI 2.6.0 (Swagger UI)
- **Build Tool:** Apache Maven

## Core API Endpoints

### Authentication (`/api/v1/auth`)
- `POST /api/v1/auth/register` - Register a new user and issue a JWT token
- `POST /api/v1/auth/login` - Authenticate user credentials and return a JWT token

### Workspaces (`/api/v1/workspaces`)
- `POST /api/v1/workspaces` - Create a workspace (automatically assigns creator as `OWNER`)
- `GET /api/v1/workspaces` - List all workspaces accessible to the authenticated user
- `POST /api/v1/workspaces/{workspaceId}/members` - Add a member to the workspace (`OWNER`/`ADMIN` only)
- `PATCH /api/v1/workspaces/{workspaceId}/members/{userId}` - Update member role (`OWNER`/`ADMIN` only)
- `DELETE /api/v1/workspaces/{workspaceId}/members/{userId}` - Remove member from workspace

### Projects (`/api/v1`)
- `POST /api/v1/workspaces/{workspaceId}/projects` - Create a project inside a workspace
- `GET /api/v1/workspaces/{workspaceId}/projects` - List all projects in a workspace
- `PUT /api/v1/projects/{projectId}` - Update project metadata (`OWNER`/`ADMIN` only)
- `DELETE /api/v1/projects/{projectId}` - Delete project (`OWNER`/`ADMIN` only)

### Tasks (`/api/v1`)
- `POST /api/v1/projects/{projectId}/tasks` - Create a task and publish `TaskAssignedEvent`
- `GET /api/v1/projects/{projectId}/tasks` - Filter tasks dynamically (`status`, `priority`, `assignedToId`, `page`, `size`, `sort`)
- `PUT /api/v1/tasks/{taskId}` - Update task details and assignee
- `PATCH /api/v1/tasks/{taskId}/status` - Transition task status with workflow validation
- `DELETE /api/v1/tasks/{taskId}` - Delete a task

## Getting Started
1. Configure your PostgreSQL database credentials and JWT secret key in `src/main/resources/application.yml`.
2. Build and run the application:
   ```bash
   mvn clean spring-boot:run