# TaskFlow

Aplicativo Android para **organizar tarefas** com **categorias**, **prioridade** (baixa, média, alta), **data limite** e status **a fazer / concluída**. Os dados ficam no dispositivo usando **Room** (SQLite).

## Funcionalidades principais

- Cadastro e listagem de **categorias**
- Cadastro de **tarefas** vinculadas a uma categoria
- Lista na tela inicial com filtros **A fazer** e **Concluídas**
- Marcar tarefa como concluída, excluir, ordenação por data e prioridade
- Destaque de cor por prioridade na linha de detalhes da tarefa

## Requisitos para compilar e rodar

| Item | Versão / observação |
|------|---------------------|
| **JDK** | **17** (recomendado; o Android Studio costuma embutir o JDK certo) |
| **Android Studio** | Versão **compatível com AGP 9.x** (por exemplo **2024.2** ou mais recente) |
| **Gradle** | **9.3.1** (vem no *wrapper* do projeto; use `./gradlew` e não instale Gradle manualmente) |
| **Android Gradle Plugin** | **9.1.0** (definido em `gradle/libs.versions.toml`) |
| **compileSdk** | **36** (API 36) |
| **minSdk** | **24** (Android 7.0+) |
| **targetSdk** | **36** |

### Kotlin e KSP

- O projeto usa **KSP** para o Room (`com.google.devtools.ksp` **2.3.5** em `gradle/libs.versions.toml`).
- **Kotlin** é gerenciado pelo conjunto de plugins do **AGP 9** (não é necessário fixar uma versão de Kotlin à mão para abrir no Android Studio atual).

## Como rodar depois de clonar

1. Clone o repositório:
   ```bash
   git clone <url-do-repositório>
   cd TaskFlow
   ```
2. Abra a pasta **`TaskFlow`** no **Android Studio**.
3. Na primeira abertura, o IDE deve sincronizar o Gradle e baixar dependências.  
   Se o arquivo **`local.properties`** não existir, o Android Studio cria um com o caminho do seu **Android SDK** (não commite esse arquivo).
4. Crie um **AVD** (emulador) com API **26+** ou conecte um aparelho com **USB debugging**.
5. Execute o app no botão **Run** ou:
   ```bash
   ./gradlew :app:assembleDebug
   ```
   O APK debug fica em `app/build/outputs/apk/debug/`.

## Estrutura relevante

- `app/src/main/java/com/prova/taskflow/` — telas e lógica (`MainActivity`, cadastros, repositórios)
- `app/src/main/java/com/prova/taskflow/data/` — entidades Room, DAOs, `AppDatabase`
- `app/src/main/res/` — layouts, strings, temas

