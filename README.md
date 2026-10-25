# Régua Virtual

Aplicativo Android em Kotlin com botão flutuante e linhas de referência sobre outros aplicativos.

## Compilar pelo GitHub
1. Crie um repositório e envie o conteúdo desta pasta para a raiz dele.
2. Abra **Actions** e selecione **Build Régua Virtual APK** (ou faça um push).
3. Quando terminar, abra a execução e baixe o artefato `regua-virtual-debug-apk`.
4. Extraia o ZIP do artefato e instale `app-debug.apk` no Android.

## Primeiro uso
Abra o app, permita **Exibir sobre outros apps** e toque para iniciar. O botão redondo `≡` abre os controles. Toque em `+ Linha` para criar linhas, selecione uma linha e use as extremidades para redimensionar. O botão flutuante pode ser arrastado.

## Observações
- Android 8.0 ou superior.
- O serviço usa uma notificação persistente enquanto a sobreposição está ativa.
- A permissão de sobreposição é necessária; o sistema abre a página de permissão.
- Este projeto ainda precisa ser compilado no GitHub Actions e testado em aparelho real. O APK não foi executado nem validado neste ambiente.
