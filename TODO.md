# TODO

## Integración de notificaciones por Telegram

Se ha añadido una notificación central de Telegram cuando se asigna un inspector a un mantenimiento preventivo:

- Configuradas las propiedades `telegram.bot-token` y `telegram.chat-id`, alimentadas por las variables de entorno `TELEGRAM_BOT_TOKEN` y `TELEGRAM_CHAT_ID`.
- Creado `TelegramNotificationService` para enviar mensajes mediante la API `sendMessage` de Telegram.
- Creado `PreventiveAssignedTelegramNotificationListener`, que escucha `PreventiveAssignedEvent` tras confirmar la transacción y envía el aviso al chat central sin afectar a la asignación si Telegram falla.
- Añadida `PreventiveAssignedTelegramNotificationListenerTest`, que comprueba el contenido del mensaje y que un error del proveedor no se propaga al flujo de asignación.
- Añadido Javadoc a los nuevos componentes de producción.

## Tareas completadas

- [x] Actualizar `docs/logica-de-negocio_0.2.docx` para documentar la nueva prueba `PreventiveAssignedTelegramNotificationListenerTest` y el flujo de notificación central por Telegram tras la asignación de un preventivo.
- [x] Actualizar `README.md` para incluir las variables de entorno de Telegram y la notificación central generada al asignar un preventivo.
- [x] Actualizar `src/test/resources/application-test.properties` con valores ficticios para `telegram.bot-token` y `telegram.chat-id`, necesarios para las pruebas con `@SpringBootTest` sin variables de entorno reales.
