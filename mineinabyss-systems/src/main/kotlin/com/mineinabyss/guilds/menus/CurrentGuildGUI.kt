package com.mineinabyss.guilds.menus

//@Composable
//fun GuiyOwner.CurrentGuildMenu(member: Player) {
//    Chest(
//        listOf(member), "${NegativeSpace.of(18)}${ChatColor.WHITE}:current_guild_menu:",
//        6, onClose = { exit() }) {
//        GuildInfoLabel(member, Modifier.at(3, 2))
//        LeaveGuildButton(member, Modifier.at(7, 4))
//    }
//}
//
//@Composable
//fun GuildInfoLabel(player: Player, modifier: Modifier) {
//    Grid(3,2, modifier){
//        repeat(6) {
//            Item(ItemStack(Material.PAPER).editItemMeta {
//                setDisplayName("${ChatColor.GOLD}${ChatColor.BOLD}Guild Name: " +
//                        "${ChatColor.YELLOW}${ChatColor.ITALIC}${player.getGuildName()}")
//                lore = listOf(
//                    "${ChatColor.GOLD}${ChatColor.BOLD}Guild Owner: ${ChatColor.YELLOW}${ChatColor.ITALIC}${player.getGuildOwner().toPlayer()?.name}",
//                    "${ChatColor.GOLD}${ChatColor.BOLD}Guild Level: ${ChatColor.YELLOW}${ChatColor.ITALIC}${player.getGuildLevel()}"
//                )
//            })
//        }
//    }
//}
//
//@Composable
//fun LeaveGuildButton(player: Player, modifier: Modifier) {
//    Grid(2, 2, modifier.clickable {
//        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_GENERIC, 1f, 1f)
//        player.leaveGuild()
//        player.closeInventory()
//    })
//    {
//        repeat(4) {
//            Item(ItemStack(Material.PAPER).editItemMeta {
//                setDisplayName("${ChatColor.RED}${ChatColor.ITALIC}Leave Guild")
//            })
//        }
//    }
//}


