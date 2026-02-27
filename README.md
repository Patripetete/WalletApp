# WalletApp

Aplicación de escritorio para la gestión de finanzas personales, desarrollada con **JavaFX** y **SQLite**.

## Características

- Múltiples cuentas bancarias y de efectivo
- Categorías de ingresos y gastos personalizables
- Gráficos de evolución y balance mensual
- Exportación de datos
- Base de datos local SQLite (sin dependencias externas)

## Tecnologías

| Capa | Tecnología |
|------|-----------|
| UI | JavaFX |
| Persistencia | SQLite (JDBC) |
| Build | Maven |
| Lenguaje | Java 17+ |

## Requisitos

- JDK 17 o superior
- Maven 3.8+

## Instalación

```bash
git clone https://github.com/Patripetete/WalletApp.git
cd WalletApp
mvn clean javafx:run
```

## Estructura del proyecto

```
src/
  main/
    java/       # Controladores y lógica de negocio
    resources/  # FXML, CSS, assets
```

---

Desarrollado por [Patricio García](https://pttstack.dev)
