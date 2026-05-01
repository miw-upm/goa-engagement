# Endpoints API (`goa-engagement`)

Listado actualizado de endpoints expuestos por los resources actuales, incluyendo DTOs HTTP y entidades de base de datos implicadas.

## SystemResource (`/system`)

| Metodo | Ruta | Metodo Java | Entrada DTO | Salida DTO | Entidad BD | Seguridad |
|---|---|---|---|---|---|---|
| GET | `/system` | `applicationInfo` | - | `ApplicationInfoDto` | - | Publica |
| GET | `/system/version-badge` | `generateBadge` | - | `byte[]` (`image/svg+xml`) | - | Publica |

## LegalTaskResource (`/legal-tasks`)

| Metodo | Ruta | Metodo Java | Entrada DTO | Salida DTO | Entidad BD | Seguridad |
|---|---|---|---|---|---|---|
| POST | `/legal-tasks` | `create` | `LegalTask` | - | `LegalTaskEntity` | `ADMIN_MANAGER_OPERATOR` |
| GET | `/legal-tasks/{id}` (`UUID`) | `read` | - | `LegalTask` | `LegalTaskEntity` | `ADMIN_MANAGER_OPERATOR` |
| PUT | `/legal-tasks/{id}` (`UUID`) | `update` | `LegalTask` | - | `LegalTaskEntity` | `ADMIN_MANAGER_OPERATOR` |
| DELETE | `/legal-tasks/{id}` (`UUID`) | `delete` | - | - | `LegalTaskEntity` | `ADMIN` |
| GET | `/legal-tasks?title={title}` | `find` | `title` (`@RequestParam`, opcional) | `List<LegalTask>` | `LegalTaskEntity` | `ADMIN_MANAGER_OPERATOR` |

## LegalProcedureTemplateResource (`/legal-procedure-templates`)

| Metodo | Ruta | Metodo Java | Entrada DTO | Salida DTO | Entidad BD | Seguridad |
|---|---|---|---|---|---|---|
| POST | `/legal-procedure-templates` | `create` | `LegalProcedureTemplate` | - | `LegalProcedureTemplateEntity`, `LegalTaskEntity` (`@DBRef`) | `ADMIN_MANAGER_OPERATOR` |
| GET | `/legal-procedure-templates/{id}` (`UUID`) | `read` | - | `LegalProcedureTemplate` | `LegalProcedureTemplateEntity`, `LegalTaskEntity` (`@DBRef`) | `ADMIN_MANAGER_OPERATOR` |
| PUT | `/legal-procedure-templates/{id}` (`UUID`) | `update` | `LegalProcedureTemplate` | - | `LegalProcedureTemplateEntity`, `LegalTaskEntity` (`@DBRef`) | `ADMIN_MANAGER_OPERATOR` |
| DELETE | `/legal-procedure-templates/{id}` (`UUID`) | `delete` | - | - | `LegalProcedureTemplateEntity` | `ADMIN` |
| GET | `/legal-procedure-templates` | `find` | `LegalProcedureTemplateFindCriteria` (`@ModelAttribute`) | `List<LegalProcedureTemplate>` | `LegalProcedureTemplateEntity`, `LegalTaskEntity` (`@DBRef`) | `ADMIN_MANAGER_OPERATOR` |

## EngagementLetterResource (`/engagement-letters`)

| Metodo | Ruta | Metodo Java | Entrada DTO | Salida DTO | Entidad BD | Seguridad |
|---|---|---|---|---|---|---|
| POST | `/engagement-letters` | `create` | `EngagementLetter` | - | `EngagementLetterEntity` | `ADMIN_MANAGER_OPERATOR` |
| GET | `/engagement-letters/{id}` (`UUID`) | `read` | - | `EngagementLetter` | `EngagementLetterEntity` | `ADMIN_MANAGER_OPERATOR` |
| GET | `/engagement-letters/{id}/view` (`UUID`) | `readPdf` | - | `byte[]` (`application/pdf`) | `EngagementLetterEntity` | `ADMIN_MANAGER_OPERATOR` |
| PUT | `/engagement-letters/{id}` (`UUID`) | `update` | `EngagementLetter` | - | `EngagementLetterEntity` | `ADMIN` |
| DELETE | `/engagement-letters/{id}` (`UUID`) | `delete` | - | - | `EngagementLetterEntity` | `ADMIN` |
| GET | `/engagement-letters` | `find` | `EngagementLetterFindCriteria` (`@ModelAttribute`) | `List<EngagementLetter>` | `EngagementLetterEntity` | `ADMIN_MANAGER_OPERATOR` |
| GET | `/engagement-letters/{id}/pending-signers` (`UUID`) | `findPendingSigners` | - | `List<UserSnapshot>` | `EngagementLetterEntity` | `ADMIN_MANAGER_OPERATOR` |
| GET | `/engagement-letters/read-engagement-letter/{urlId}/{token}` | `readPdfWithToken` | - | `byte[]` (`application/pdf`) | `EngagementLetterEntity`, `CustomerFileDownloadEntity` | `ALL` (`permitAll` en filter chain) |
| GET | `/engagement-letters/sign-engagement-letter/{urlId}/{token}` | `readBeforeSigningWithToken` | - | `byte[]` (`application/pdf`) | `EngagementLetterEntity`, `CustomerFileDownloadEntity` | `ALL` (`permitAll` en filter chain) |
| PATCH | `/engagement-letters/sign-engagement-letter/{urlId}/{token}` | `signWithToken` | `AcceptanceEngagementCreationDto` | - | `EngagementLetterEntity` (`acceptanceEngagementEntities`) | `ALL` (`permitAll` en filter chain) |

Notas:
- `read-engagement-letter` y `sign-engagement-letter` consumen token URL y generan trazabilidad de descarga (`customer-file-downloads`).
- `PATCH /sign-engagement-letter/...` firma un interviniente y actualiza la carta de encargo.

## CustomerFileDownloadResource (`/customer-file-downloads`)

| Metodo | Ruta | Metodo Java | Entrada DTO | Salida DTO | Entidad BD | Seguridad |
|---|---|---|---|---|---|---|
| GET | `/customer-file-downloads/{id}` (`UUID`) | `read` | - | `CustomerFileDownload` | `CustomerFileDownloadEntity` | `ADMIN_MANAGER_OPERATOR` |
| GET | `/customer-file-downloads` | `find` | `CustomerFileDownloadFindCriteria` (`@ModelAttribute`) | `List<CustomerFileDownload>` | `CustomerFileDownloadEntity` | `ADMIN_MANAGER_OPERATOR` |

## Contenido DTO y comparativa con Entity

### `AcceptanceEngagementCreationDto` vs `AcceptanceEngagementEntity`

| Campo DTO | Tipo DTO | Campo Entity | Tipo Entity | Nota |
|---|---|---|---|---|
| `documentAccepted` | `Boolean` | `documentAccepted` | `Boolean` | Se copia al crear firma |
| `signature` | `String` | - | - | En el flujo actual no se persiste en Mongo |
| - | - | `signatureAt` | `LocalDateTime` | Lo completa backend |
| - | - | `signerId` | `UUID` | Lo completa backend |
| - | - | `signerFullName` | `String` | Lo completa backend |
| - | - | `signerIdentity` | `String` | Lo completa backend (se codifica antes de guardar) |
| - | - | `mobile` | `String` | Lo completa backend |
| - | - | `signerEmail` | `String` | Lo completa backend (se codifica antes de guardar) |
| - | - | `signatureToken` | `String` | Lo completa backend (se codifica antes de guardar) |
| - | - | `deviceInfo` | `DeviceInfo` | Resuelto desde request HTTP |

### `CustomerFileDownload` vs `CustomerFileDownloadEntity`

| Campo DTO | Tipo DTO | Campo Entity | Tipo Entity | Nota |
|---|---|---|---|---|
| `id` | `UUID` | `id` | `UUID` | |
| `downloadedAt` | `LocalDateTime` | `downloadedAt` | `LocalDateTime` | |
| `customer.id` | `UUID` | `customerId` | `UUID` | Snapshot en DTO, id plano en entity |
| `documentType` | `String` | `documentType` | `String` | |
| `documentId` | `UUID` | `documentId` | `UUID` | |
| `downloadToken` | `String` | `downloadToken` | `String` | Se guarda codificado |

### `LegalTask` vs `LegalTaskEntity`

| Campo DTO | Tipo DTO | Campo Entity | Tipo Entity | Nota |
|---|---|---|---|---|
| `id` | `UUID` | `id` | `UUID` | |
| `title` | `String` | `title` | `String` | `@Indexed(unique = true)` en entity |

### `LegalProcedureTemplate` vs `LegalProcedureTemplateEntity`

| Campo DTO | Tipo DTO | Campo Entity | Tipo Entity | Nota |
|---|---|---|---|---|
| `id` | `UUID` | `id` | `UUID` | |
| `title` | `String` | `title` | `String` | `@Indexed(unique = true)` en entity |
| `budget` | `BigDecimal` | `budget` | `BigDecimal` | |
| `legalTasks` | `List<LegalTask>` | `legalTaskEntities` | `List<LegalTaskEntity>` | Relacion por `@DBRef` |

### `EngagementLetter` vs `EngagementLetterEntity`

| Campo DTO | Tipo DTO | Campo Entity | Tipo Entity | Nota |
|---|---|---|---|---|
| `id` | `UUID` | `id` | `UUID` | |
| `budgetOnly` | `Boolean` | `budgetOnly` | `Boolean` | |
| `lastUpdatedDate` | `LocalDate` | `lastUpdatedDate` | `LocalDate` | |
| `discount` | `Integer` | `discount` | `Integer` | |
| `closingDate` | `LocalDate` | `closingDate` | `LocalDate` | |
| `owner.id` | `UUID` | `ownerId` | `UUID` | Snapshot en DTO, id plano en entity |
| `attachments[].id` | `List<UUID>` | `attachmentIds` | `List<UUID>` | Snapshot en DTO, ids planos en entity |
| `legalProcedures` | `List<LegalProcedure>` | `legalProcedureEntities` | `List<LegalProcedureEntity>` | |
| `paymentMethods` | `List<PaymentMethod>` | `paymentMethodEntities` | `List<PaymentMethodEntity>` | |
| `legalClause` | `String` | `legalClause` | `String` | |
| `acceptanceEngagements` | `List<AcceptanceEngagement>` | `acceptanceEngagementEntities` | `List<AcceptanceEngagementEntity>` | |
