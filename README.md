# Lol Manager - API de Gestión de Torneos

> [!IMPORTANT]
> **Estado del Proyecto**: En desarrollo

## Descripción
Este proyecto es una API REST diseñada para facilitar la organización y gestión de competiciones de League of Legends. Permite a los usuarios registrarse, verificar sus cuentas de Riot, formar equipos, buscar jugadores libres y participar en torneos con generación automática de enfrentamientos.

# Documentación de la API

Este documento detalla los endpoints disponibles en la aplicacion.

## AuthController (/api/auth)

Controlador para la autenticacion y registro de usuarios.

*   **POST** `/api/auth/register`
    *   **Descripcion**: Registra un nuevo usuario en el sistema.
    *   **Body**: Objeto `User` (email, password, nickname).
    *   **Retorno**: `201 Created` (Sin cuerpo).

*   **POST** `/api/auth/{userId}/link/start`
    *   **Descripcion**: Inicia el proceso de vinculacion de una cuenta de Riot (LoL) con el usuario.
    *   **Path Variable**: `userId` (ID del usuario).
    *   **Query Params**: `name` (Nombre de invocador), `tag` (Tagline).
    *   **Retorno**: Objeto `Player` con los datos preliminares y el icono requerido para verificacion.

*   **POST** `/api/auth/{userId}/link/verify`
    *   **Descripcion**: Verifica si el usuario cambio su icono de invocador para confirmar la propiedad de la cuenta.
    *   **Path Variable**: `userId` (ID del usuario).
    *   **Retorno**: Mensaje de exito o error.

## PlayerController (/api/players)

Controlador para la gestion de jugadores y agentes libres.

*   **GET** `/api/players/search`
    *   **Descripcion**: Busca agentes libres (jugadores sin equipo) filtrando por rol o rango.
    *   **Query Params**: `role` (Opcional), `rank` (Opcional).
    *   **Retorno**: Lista de `FreeAgentDTO`.

*   **PUT** `/api/players/discord`
    *   **Descripcion**: Actualiza el usuario de Discord del jugador.
    *   **Query Params**: `userId`, `discord` (Nuevo usuario de Discord).
    *   **Retorno**: Mensaje de confirmacion.

*   **PUT** `/api/players/roles`
    *   **Descripcion**: Actualiza los roles preferidos del jugador.
    *   **Query Params**: `userId`, `primary` (Rol primario), `secondary` (Rol secundario).
    *   **Retorno**: Mensaje de confirmacion.

*   **POST** `/api/players/refresh-ranks`
    *   **Descripcion**: Actualiza los rangos del jugador consultando nuevamente la API de Riot.
    *   **Query Params**: `userId`.
    *   **Retorno**: Mensaje de confirmacion.

## TeamController (/api/teams)

Controlador para la gestion de equipos.

*   **GET** `/api/teams`
    *   **Descripcion**: Obtiene una lista de todos los equipos registrados.
    *   **Retorno**: Lista de objetos `Team`.

*   **POST** `/api/teams`
    *   **Descripcion**: Crea un nuevo equipo.
    *   **Query Params**: `userId` (ID del capitan/creador).
    *   **Body**: Objeto `Team` (nombre, tag).
    *   **Retorno**: El objeto `Team` creado.

*   **POST** `/api/teams/{teamId}/join-request`
    *   **Descripcion**: Envia una solicitud para unirse a un equipo.
    *   **Path Variable**: `teamId`.
    *   **Query Params**: `userId`.
    *   **Retorno**: Mensaje de confirmacion.

*   **GET** `/api/teams/{teamId}/requests`
    *   **Descripcion**: Obtiene las solicitudes de union pendientes de un equipo (Solo lider).
    *   **Path Variable**: `teamId`.
    *   **Query Params**: `userId`.
    *   **Retorno**: Lista de `InvitationResponseDTO`.

## InvitationController (/api/invitations)

Controlador para gestionar invitaciones a equipos.

*   **POST** `/api/invitations/invite/{targetPlayerId}`
    *   **Descripcion**: Envia una invitacion a un jugador para unirse al equipo del usuario. Genera una notificacion al usuario invitado.
    *   **Path Variable**: `targetPlayerId` (ID del jugador a invitar).
    *   **Query Params**: `myUserId` (ID del usuario que envia la invitacion).
    *   **Retorno**: Mensaje de confirmacion.

*   **GET** `/api/invitations/pending`
    *   **Descripcion**: Obtiene las invitaciones pendientes de un usuario.
    *   **Query Params**: `userId`.
    *   **Retorno**: Lista de `InvitationResponseDTO`.

*   **POST** `/api/invitations/{invitationId}/respond`
    *   **Descripcion**: Acepta o rechaza una invitacion.
    *   **Path Variable**: `invitationId`.
    *   **Query Params**: `userId`, `accept` (booleano true/false).
    *   **Retorno**: Mensaje con el resultado de la operacion.

*   **POST** `/api/invitations/{invitationId}/manage`
    *   **Descripcion**: Acepta o rechaza una solicitud de union a un equipo (Accion del lider).
    *   **Path Variable**: `invitationId`.
    *   **Query Params**: `userId`, `accept` (booleano).
    *   **Retorno**: Mensaje con el resultado.

## TournamentController (/api/tournaments)

Controlador principal para la gestion de torneos y partidos.

*   **GET** `/api/tournaments`
    *   **Descripcion**: Obtiene todos los torneos.
    *   **Retorno**: Lista de `TournamentDTO`.

*   **POST** `/api/tournaments`
    *   **Descripcion**: Crea un nuevo torneo (Estado borrador).
    *   **Body**: `TournamentDTO` (Incluye `maxTeams`: 8/16/32 y `matchmakingStrategy`: RANDOM/BALANCED/COMPETITIVE).
    *   **Retorno**: `TournamentDTO` creado.

*   **POST** `/api/tournaments/{tournamentId}/register`
    *   **Descripcion**: Inscribe un equipo a un torneo en etapa de registro.
    *   **Path Variable**: `tournamentId`.
    *   **Query Params**: `teamId`.
    *   **Retorno**: Mensaje de confirmacion.

*   **POST** `/api/tournaments/{tournamentId}/start`
    *   **Descripcion**: Inicia el torneo, generando el fixture (brackets) y los partidos.
    *   **Path Variable**: `tournamentId`.
    *   **Retorno**: Mensaje de confirmacion.

*   **GET** `/api/tournaments/{tournamentId}/matches`
    *   **Descripcion**: Obtiene la lista de partidos (fixture) de un torneo.
    *   **Path Variable**: `tournamentId`.
    *   **Retorno**: Lista de `MatchDTO`.

*   **GET** `/api/tournaments/match/{matchId}`
    *   **Descripcion**: Obtiene los detalles de un partido especifico.
    *   **Path Variable**: `matchId`.
    *   **Retorno**: `MatchDTO`.

*   **POST** `/api/tournaments/match/{matchId}/report`
    *   **Descripcion**: Reporta el resultado de un partido. Requiere confirmacion de ambos equipos o espera por autovictoria. Si es el ultimo partido, finaliza el torneo.
    *   **Path Variable**: `matchId`.
    *   **Query Params**: `myTeamId` (ID del equipo que reporta), `winnerId` (ID del equipo ganador segun el reporte).
    *   **Retorno**: Mensaje sobre el estado del reporte (Registrado/Confirmado/Disputa).

*   **POST** `/api/tournaments/matches/{matchId}/schedule`
    *   **Descripcion**: Programa el horario de un partido especifico (dentro de los 14 dias del inicio del torneo).
    *   **Path Variable**: `matchId`.
    *   **Query Params**: `userId`.
    *   **Body**: Fecha y hora programada (`LocalDateTime`).
    *   **Retorno**: Mensaje de confirmacion.

*   **GET** `/api/tournaments/{tournamentId}/brackets`
    *   **Descripcion**: Obtiene la estructura de brackets del torneo.
    *   **Path Variable**: `tournamentId`.
    *   **Retorno**: `TournamentBracketDTO`.

## MatchController (/api/matches)

Controlador para la gestion operativa de los partidos (Check-in, Walkover, Admin).

*   **POST** `/api/matches/{matchId}/check-in`
    *   **Descripcion**: Realiza el check-in (dar el presente) para un partido. Habilitado 20 minutos antes del horario programado.
    *   **Path Variable**: `matchId`.
    *   **Query Params**: `userId`.
    *   **Retorno**: Mensaje de confirmacion.

*   **POST** `/api/matches/{matchId}/walkover`
    *   **Descripcion**: Reclama la victoria automatica (Walkover) si el rival no hizo check-in y ya paso el horario del partido.
    *   **Path Variable**: `matchId`.
    *   **Query Params**: `userId`.
    *   **Retorno**: Mensaje de confirmacion y victoria asignada.

*   **POST** `/api/matches/{matchId}/admin`
    *   **Descripcion**: Solicita la intervencion de un administrador (Disputa). Notifica a todos los admins.
    *   **Path Variable**: `matchId`.
    *   **Query Params**: `userId`.
    *   **Retorno**: Mensaje de confirmacion.

*   **POST** `/api/matches/{matchId}/resolve`
    *   **Descripcion**: Resuelve manualmente un partido en disputa (Solo Administrador).
    *   **Path Variable**: `matchId`.
    *   **Query Params**: `winnerId`, `adminId`.
    *   **Retorno**: Mensaje de resolucion.

## NotificationController (/api/notifications)

Controlador para la gestion de notificaciones de usuario.

*   **GET** `/api/notifications/unread/{userId}`
    *   **Descripcion**: Obtiene las notificaciones no leidas de un usuario.
    *   **Path Variable**: `userId`.
    *   **Retorno**: Lista de `Notification`.

*   **POST** `/api/notifications/{id}/read`
    *   **Descripcion**: Marca una notificacion especifica como leida.
    *   **Path Variable**: `id` (ID de la notificacion).
    *   **Retorno**: Mensaje de confirmacion.

## AccountRiotController (/api/account-riot)

Controlador de utilidad para probar conexion con API de Riot.

*   **GET** `/api/account-riot`
    *   **Descripcion**: Busca una cuenta de Riot directamente.
    *   **Query Params**: `name`, `tag`.
    *   **Retorno**: `RiotAccountDTO`.

## UserController (/api/users)

Controlador para la gestión de perfiles de usuario.

*   **GET** `/api/users/{userId}/profile`
    *   **Descripcion**: Obtiene el perfil completo de un usuario, incluyendo datos de jugador y estadísticas.
    *   **Path Variable**: `userId`.
    *   **Retorno**: `UserProfileDTO`.
