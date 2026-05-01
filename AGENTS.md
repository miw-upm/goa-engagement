# Guia de estilo y arquitectura - GOA Engagement (v1)

Documento normativo para contribuir en `goa-engagement`.
Esta version refleja el estado real del codigo (auditoria: 2026-05-01).

## Niveles de regla

- `DEBE`: obligatorio.
- `DEBERIA`: recomendado, salvo razon tecnica explicita.
- `PUEDE`: opcional.

## Arquitectura general

Sistema de microservicios con Eureka + API Gateway + servicios Spring Boot.

```text
eureka
  ->
gateway
  ->
microservicios (goa-engagement, goa-user, goa-support, ...)
  <-
commons (goa-commons)
```

## Estructura real del microservicio

```text
es.upm.api/
  adapter/
    in/
      resources/
        dtos/
        httperrors/
    out/
      legal/mongo/
      user/feign/
      email/feign/
  configurations/
  domain/
    model/
      criteria/
      external/
    ports/out/
    services/
```

Notas:
- El proyecto usa un enfoque de puertos y adaptadores.
- No hay capa `infrastructure` separada: los adaptadores de salida estan en `adapter.out`.

## Resources HTTP (`adapter.in.resources`)

- DEBE usar `@RestController` y sufijo `Resource`.
- DEBE delegar logica de negocio a `domain.services`.
- DEBE definir rutas base y subrutas como constantes `public static final String`.
- DEBE usar inyeccion por constructor con `@RequiredArgsConstructor`.
- DEBE aplicar seguridad con `@PreAuthorize` y constantes de `es.upm.miw.security.Security`.
- NO DEBE usar expresiones SpEL literales en anotaciones de seguridad.
- DEBERIA usar `@Valid` en cuerpos de entrada cuando aplique.
- DEBERIA usar `@ModelAttribute` para criterios de busqueda.

Resources actuales:
- `SystemResource`
- `LegalTaskResource`
- `LegalProcedureTemplateResource`
- `EngagementLetterResource`
- `CustomerFileDownloadResource`

## DTOs y contratos HTTP

- DEBE ubicar DTOs HTTP explicitos en `adapter.in.resources.dtos`.
- DEBE mantener DTOs de entrada separados de modelos de dominio cuando exista flujo publico con requisitos propios.
- DEBERIA evitar logica de negocio en DTOs.

DTOs HTTP locales actuales:
- `AcceptanceEngagementCreationDto`

DTOs externos usados desde commons:
- `ApplicationInfoDto` (`/system`)

## Dominio (`domain`)

- DEBE ubicar modelo de negocio en `domain.model`.
- DEBE ubicar filtros de busqueda en `domain.model.criteria` con sufijo `FindCriteria`.
- DEBE ubicar snapshots de otros microservicios en `domain.model.external`.
- DEBE ubicar interfaces de salida en `domain.ports.out`.
- DEBE ubicar casos de uso en `domain.services`.

Modelos principales:
- `EngagementLetter`
- `LegalProcedureTemplate`
- `LegalTask`
- `CustomerFileDownload`
- `AcceptanceEngagement`
- `PaymentMethod`
- `LegalProcedure`

## Servicios (`domain.services`)

- DEBE usar `@Service` y sufijo `Service`.
- DEBE trabajar con modelo de dominio, no con entidades Mongo.
- DEBE acceder a infraestructura via puertos (`domain.ports.out`), no via repositories concretos.
- DEBERIA mantener nombres de operaciones consistentes: `create`, `read`, `update`, `delete`, `find`.
- DEBE generar IDs de agregados en capa de servicio cuando el flujo lo requiera.
- DEBE encapsular reglas de negocio en metodos privados cuando la regla no sea trivial.
- DEBE codificar datos sensibles antes de persistirlos (ejemplo: token de firma/descarga, identidad, email, IP).

## Persistencia Mongo (`adapter.out.legal.mongo`)

- DEBE usar entidades con sufijo `Entity`.
- DEBE marcar colecciones con `@Document` e IDs con `@Id`.
- DEBERIA usar `@Indexed(unique = true)` para campos de unicidad funcional.
- PUEDE usar `@DBRef` cuando la relacion lo exija (ej. tareas en plantilla de procedimientos).
- DEBE mapear `Entity <-> Domain` dentro de la propia entidad/adaptador.
- DEBERIA reutilizar `BeanUtils.copyProperties` donde encaje con el modelo actual.

Regla de adaptadores:
- DEBE implementar los puertos de `domain.ports.out`.
- DEBE traducir errores de acceso en excepciones de dominio (`NotFoundException`, `ConflictException`, etc.).

## Integraciones externas (`adapter.out.user.feign`, `adapter.out.email.feign`)

- DEBE aislar llamadas a otros microservicios en clientes/adaptadores Feign.
- DEBE mantener contratos remotos fuera de servicios de dominio.
- DEBE propagar `Authorization` con `FeignConfig`:
  - token del contexto JWT si existe;
  - token tecnico via `TokenManager` en caso contrario.

Clientes actuales:
- `GoaUserClient` (usuarios y access links)
- `GoaSupportClient` (envio de email HTML)

## Seguridad

- DEBE mantener coherencia entre:
  - reglas `@PreAuthorize` en resources;
  - reglas de `SecurityFilterChain` en `ResourceServerConfig`.
- DEBE mantener `SessionCreationPolicy.STATELESS`.
- DEBE permitir solo endpoints publicos definidos explicitamente (`/system`, docs OpenAPI, y rutas tokenizadas de lectura/firma).
- DEBERIA revisar cualquier nueva ruta publica en ambos niveles (filtro + metodo).

## Excepciones y manejo de errores

- DEBE usar excepciones de dominio de `es.upm.miw.exception`.
- DEBE centralizar mapeo HTTP en `adapter.in.resources.httperrors.ApiExceptionHandler`.
- NO DEBE introducir manejo de errores ad hoc en cada resource salvo casos puntuales.

## Configuracion y perfiles

- DEBE ubicar configuracion tecnica en `configurations`.
- DEBE usar perfiles para datos de prueba:
  - `DatabaseSeederDev` solo en `dev` y `test`.
- DEBERIA mantener IDs semiestables en seeder cuando los tests dependan de datos iniciales.

## Testing

Convencion actual:
- Integracion: `*IT`
- Unitario/repo puntual: `*Test`
- Verificacion puntual de PDF: `EngagementLetterPdfCheck`

Reglas:
- DEBE cubrir casos felices y de error.
- DEBE incluir pruebas de seguridad/autorizacion en endpoints sensibles.
- DEBERIA mockear dependencias remotas (`GoaUserClient`, `GoaSupportClient`) en IT de servicio/resource.
- DEBERIA evitar dependencia temporal fragil (`now`) salvo que sea necesaria por comportamiento.

## Tecnologia y build

- Java objetivo: **21**
- Spring Boot: **3.5.x** (actual: `3.5.9`)
- Spring Cloud: **2025.0.x** (actual: `2025.0.1`)
- Base de datos: MongoDB
- Cliente HTTP interno: OpenFeign
- Documentacion API: springdoc OpenAPI

Reglas de build:
- DEBE compilarse con JDK 21.
- DEBERIA mantener annotation processing de Lombok correctamente habilitado en IDE/CI.

## Antipatrones prohibidos

- Logica de negocio en resources o en entities Mongo.
- Acceso directo a `MongoRepository` desde `domain.services`.
- SpEL literal en `@PreAuthorize`.
- Mezclar contratos de microservicios externos dentro del modelo de dominio.
- Exponer datos sensibles en claro despues de procesos de firma/descarga.
