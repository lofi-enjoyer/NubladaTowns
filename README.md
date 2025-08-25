# NubladaTowns  

Protect your land without any command, just using vanilla items and in-game actions!

![imagen](https://github.com/user-attachments/assets/91209640-f229-4b36-ab9e-2965b2a1eee5)

## Features
- Command-less town management without custom items or resource packs
- Protection against block breaking, placing, explosions and animal damage
- Role and permission system
- In-game maps integration
- Power system thorugh mob killing
- 100% translatable
- PlaceholderAPI integration

## How to start

### Creating a town

1. Create a custom banner and make some copies, you will need them later.
2. Rename the banner with the name you want to give to your town.
3. Place the banner where you want to found it. It will create the town and claim the chunk the banner was placed in.
4. Now you can manage your town through the lectern that just appeared.

![found](https://github.com/user-attachments/assets/a1991749-403a-4297-9961-707b622981e8)

### Claiming land

1. Rename one of the banners you copied earlier to the name of the town.
2. Make sure you have enough power to claim (each server may require a different amount). Power is earned through killing mobs.
3. Place the banner on the land you want to claim. Keep in mind it has to be adjacent to land you already own.

![claim](https://github.com/user-attachments/assets/8502cd66-c4d0-43c1-a931-022d4a7e595f)

### Creating a town role
1. Rename a paper with the name of the role
2. Right click the town lectern with it
3. Use the lectern's role menu to edit its permissions
4. Assign it to a resident through the lectern's resident menu

### Moving the town's lectern
1. Rename a lectern with the town's name
2. Place it where you want it to be. The previous one will be removed.

## Extra information
- You can see claimed land using maps
- If you have a compass on your inventory you will be notified when entering/exiting a town's territory, and you will be able to see nearby borders if you have it on your main hand

## Commands & Permissions
Although players do not directly use commands, menus do. Also, there are some commands for administrators.

| Command | Description | Permission | Default |
| --- | --- | --- | --- |
| /t  | Internally used by town menus | nubladatowns.user | `true` |
| /nta  | Used by administrators | nubladatowns.admin | `op` |
| /nta info | Information about the specified town | nubladatowns.admin | `op` |
| /nta tp | Teleports the user to the specified town | nubladatowns.admin | `op` |
| /nta power | Manages towns' power | nubladatowns.admin | `op` |
| /nta claim | Claims the current chunk for a town | nubladatowns.admin | `op` |
| /nta abandon | Removes the current chunk from the town that has it | nubladatowns.admin | `op` |
| /nta delete | Deletes the specified town | nubladatowns.admin | `op` |
| /nta load | Loads the plugin data. **WARNING: This will erase the changes since the last load, so make sure to save first to avoid losing data** | nubladatowns.admin | `op` |
| /nta save | Saves the plugin data | nubladatowns.admin | `op` |
| /nta reload | Reloads the plugin's configuration and language files | nubladatowns.admin | `op` |

## PlaceholderAPI
| Identifier | Description |
| --- | --- |
| nubladatowns_town | Player town. |
| nubladatowns_power | Player town's power. |
| nubladatowns_mayor | Player town's mayor. |
| nubladatowns_residents_amount | Player town's residents. |
| nubladatowns_claimed_land_amount | Player town's claimed land. |
| nubladatowns_town_spawn | Player town's spawn coordinates. |
| nubladatowns_town_spawn_x | Player town's spawn X coordinate. |
| nubladatowns_town_spawn_y | Player town's spawn Y coordinate. |
| nubladatowns_town_spawn_z | Player town's spawn Z coordinate. |
| nubladatowns_town_color_hex | Player town's color in hex format. |
| nubladatowns_town_is_open | Player town's open status. |
| nubladatowns_has_town | `true` if the player is part of a town, otherwise `false` |
