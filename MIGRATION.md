# MM-Wiki Java Migration

## Goal

Keep the original MM-Wiki product behavior, route shape, table structure, page style, and front-end scripts as stable as possible while replacing the Go/Beego backend with Spring Boot 3.5 + Java 21 + MyBatis + Thymeleaf + Spring Session JDBC.

## Current Status

The following foundation is already in place:

- Spring Boot MVC + Thymeleaf bootstrapped
- MyBatis mapper scanning enabled
- Spring Session JDBC enabled
- Legacy static assets copied to `src/main/resources/static`
- Legacy table DDL copied to `src/main/resources/db/mmwiki-schema.sql`
- Compatible auth flow implemented for:
  - `/author/index`
  - `/author/login`
  - `/author/logout`
  - `/author/authLogin` (temporary stub)
- Compatible cookie/session validation interceptor implemented
- First migrated pages implemented:
  - `/`
  - `/main/index`
  - `/main/default`
  - `/main/about`

## Old-to-New Module Mapping

- `app/controllers/author.go`
  - `org.tinycloud.mmwiki.controller.AuthorController`
- `app/controllers/main.go`
  - `org.tinycloud.mmwiki.controller.MainController`
- `app/models/user.go`
  - `org.tinycloud.mmwiki.mapper.UserMapper`
  - `org.tinycloud.mmwiki.service.UserService`
- `app/models/config.go`
  - `org.tinycloud.mmwiki.mapper.ConfigMapper`
  - `org.tinycloud.mmwiki.service.ConfigService`
- `app/models/collection.go`
  - `org.tinycloud.mmwiki.mapper.CollectionMapper`
- `app/models/document.go`
  - `org.tinycloud.mmwiki.mapper.DocumentMapper`
- `app/models/log_document.go`
  - `org.tinycloud.mmwiki.mapper.LogDocumentMapper`
- `app/models/link.go`
  - `org.tinycloud.mmwiki.mapper.LinkMapper`
- `app/models/contact.go`
  - `org.tinycloud.mmwiki.mapper.ContactMapper`

## Compatibility Rules

- Keep original table names like `mw_user`, `mw_document`, `mw_space`
- Keep original route paths where practical
- Keep login password behavior compatible with old front-end:
  - browser sends `SHA-256(password)`
  - server compares `MD5(sha256Password)`
- Keep custom `passport` cookie behavior compatible with old session checks
- Reuse original static resource paths under `/static/**`

## Next Priority

1. Migrate `space` module
2. Migrate `document` browsing/editing module
3. Migrate `attachment` upload/download module
4. Migrate `user` module
5. Migrate `system` admin module
6. Reintroduce document file storage abstraction
7. Reintroduce search/index capability

## Suggested Migration Strategy

Migrate by vertical slice instead of by layer only:

1. Pick one old controller group
2. Port related mapper queries first
3. Port service logic second
4. Port matching Thymeleaf templates with the same DOM structure
5. Compile and verify before moving to the next slice

