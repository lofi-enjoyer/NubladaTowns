# NubladaTowns

Protect your land without any command, just using vanilla items and in-game actions!

![imagen](https://github.com/user-attachments/assets/91209640-f229-4b36-ab9e-2965b2a1eee5)

## Features

- Command-less town management without custom items or resource packs
- Protection against block breaking, placing, explosions and animal damage
- Role and permission system
- In-game maps integration
- Power system through mob killing
- 100% translatable
- PlaceholderAPI integration
- Town invitation system
- Town inventory system
- Town history tracking

## Manual de Uso

### Creación de un Town

1. Crea un banner personalizado y haz algunas copias (las necesitarás más tarde)
2. Renombra el banner con el nombre que quieras darle a tu town
3. Coloca el banner donde quieras fundar el town. Esto creará el town y reclamará el chunk donde se colocó el banner
4. Un lectern aparecerá automáticamente, que será el centro de operaciones de tu town

### Reclamar Territorio

1. Renombra uno de los banners copiados con el nombre del town
2. Asegúrate de tener suficiente poder para reclamar (cada servidor puede requerir una cantidad diferente)
3. Coloca el banner en el territorio que quieras reclamar. Debe ser adyacente a territorio que ya poseas

### Sistema de Invitaciones

1. Para invitar a un jugador:
   - Ve al lectern del town
   - Ten un libro en la mano
   - Necesitas 5 niveles de experiencia (configurable)
   - Debes tener el permiso de invitar en el town
2. El jugador invitado debe:
   - Ir al lectern del town
   - Tener el libro de invitación en la mano
   - No estar en ningún otro town
   - Estar a menos de 5 bloques del lectern

### Sistema de Roles

1. Crear un rol:
   - Renombra un papel con el nombre del rol
   - Haz clic derecho en el lectern del town con el papel
   - Usa el menú de roles del lectern para editar sus permisos
2. Asignar un rol:
   - Usa el menú de residentes del lectern
   - Selecciona el jugador
   - Asigna el rol deseado

### Gestión del Town

1. Mover el lectern:
   - Renombra un lectern con el nombre del town
   - Colócalo donde quieras que esté
2. Cambiar el banner:
   - Renombra un banner con el nombre del town
   - Haz clic derecho en el lectern con el banner
3. Ver el inventario del town:
   - Usa el menú del lectern
   - Accede a la opción de inventario

### Sistema de Poder

- El poder se obtiene matando mobs
- Cada mob da una cantidad diferente de poder
- El poder se usa para reclamar territorio
- El poder máximo está limitado por el número de residentes

### Protección

- Los towns protegen contra:
  - Romper bloques
  - Colocar bloques
  - Explosiones
  - Daño a animales
- La protección se aplica automáticamente en todo el territorio del town

### Navegación

- Usa una brújula para:
  - Ver las fronteras del town
  - Recibir notificaciones al entrar/salir de towns
- Usa mapas para ver el territorio reclamado

## Comandos & Permisos

Aunque los jugadores no usan comandos directamente, los menús sí los usan internamente. También hay comandos para administradores.

| Comando      | Descripción                                                                                 | Permiso            | Por defecto |
| ------------ | ------------------------------------------------------------------------------------------- | ------------------ | ----------- |
| /t           | Usado internamente por los menús del town                                                   | nubladatowns.user  | `true`      |
| /nta         | Usado por administradores                                                                   | nubladatowns.admin | `op`        |
| /nta info    | Información sobre un town específico                                                        | nubladatowns.admin | `op`        |
| /nta tp      | Teletransporta al usuario al town especificado                                              | nubladatowns.admin | `op`        |
| /nta power   | Gestiona el poder de los towns                                                              | nubladatowns.admin | `op`        |
| /nta claim   | Reclama el chunk actual para un town                                                        | nubladatowns.admin | `op`        |
| /nta abandon | Elimina el chunk actual del town que lo posee                                               | nubladatowns.admin | `op`        |
| /nta delete  | Elimina el town especificado                                                                | nubladatowns.admin | `op`        |
| /nta load    | Carga los datos del plugin. **ADVERTENCIA: Esto borrará los cambios desde la última carga** | nubladatowns.admin | `op`        |
| /nta save    | Guarda los datos del plugin                                                                 | nubladatowns.admin | `op`        |
| /nta reload  | Recarga la configuración y archivos de idioma                                               | nubladatowns.admin | `op`        |

## PlaceholderAPI

| Identificador                    | Descripción                                         |
| -------------------------------- | --------------------------------------------------- |
| nubladatowns_town                | Town del jugador                                    |
| nubladatowns_power               | Poder del town del jugador                          |
| nubladatowns_mayor               | Alcalde del town del jugador                        |
| nubladatowns_residents_amount    | Número de residentes del town                       |
| nubladatowns_claimed_land_amount | Cantidad de territorio reclamado                    |
| nubladatowns_town_spawn          | Coordenadas del spawn del town                      |
| nubladatowns_town_spawn_x        | Coordenada X del spawn                              |
| nubladatowns_town_spawn_y        | Coordenada Y del spawn                              |
| nubladatowns_town_spawn_z        | Coordenada Z del spawn                              |
| nubladatowns_town_color_hex      | Color del town en formato hexadecimal               |
| nubladatowns_town_is_open        | Estado de apertura del town                         |
| nubladatowns_has_town            | `true` si el jugador está en un town, `false` si no |
