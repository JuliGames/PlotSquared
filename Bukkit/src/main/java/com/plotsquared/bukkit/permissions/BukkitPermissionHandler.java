/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2021 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.bukkit.permissions;

import com.google.common.collect.Lists;
import com.plotsquared.bukkit.player.BukkitPlayer;
import com.plotsquared.core.permissions.ConsolePermissionProfile;
import com.plotsquared.core.permissions.PermissionHandler;
import com.plotsquared.core.permissions.PermissionProfile;
import com.plotsquared.core.player.ConsolePlayer;
import com.plotsquared.core.player.OfflinePlotPlayer;
import com.plotsquared.core.player.PlotPlayer;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class BukkitPermissionHandler implements PermissionHandler {

    @Override
    public void initialize() {
        System.out.println("P2 - BUKKIT PERMS");
    }

    @NonNull
    @Override
    public Optional<PermissionProfile> getPermissionProfile(
            @NonNull PlotPlayer<?> playerPlotPlayer
    ) {
        if (playerPlotPlayer instanceof final BukkitPlayer bukkitPlayer) {
            return Optional.of(new BukkitPermissionProfile(bukkitPlayer.getPlatformPlayer()));
        } else if (playerPlotPlayer instanceof ConsolePlayer) {
            return Optional.of(ConsolePermissionProfile.INSTANCE);
        }
        return Optional.empty();
    }

    @NonNull
    @Override
    public Optional<PermissionProfile> getPermissionProfile(
            @NonNull OfflinePlotPlayer offlinePlotPlayer
    ) {
        return Optional.empty();
    }

    @NonNull
    @Override
    public Set<PermissionHandlerCapability> getCapabilities() {
        return EnumSet.of(PermissionHandlerCapability.ONLINE_PERMISSIONS);
    }


    private static final class BukkitPermissionProfile implements PermissionProfile {

        private final WeakReference<Player> playerReference;

        private BukkitPermissionProfile(final @NonNull Player player) {
            this.playerReference = new WeakReference<>(player);
        }

        @Override
        public boolean hasPermission(
                final @Nullable String world,
                final @NonNull String permission
        ) {
            final Player player = this.playerReference.get();
            if(player == null) return false;

            // FIXED PERMISSION CHECK BY ROOOT
            if(player.hasPermission(permission)) return true;
            if(!permission.contains(".")) return false;
            String[] parts = permission.split("\\.");
            boolean has = false;

            ArrayList<String> perms2check = new ArrayList<>();

            for (int i = 0; i < parts.length; i++) {
                String part = parts[i];

                if(i > 0) {
                    String[] nodesBefore = Arrays.copyOfRange(parts, 0, i);

                    ArrayList<String> nodes = new ArrayList<>(Arrays.asList(nodesBefore));
                    String node = "";
                    for (final String nd : nodes) {
                        node = node + nd+".";
                    }

                    nodes.add(part);

                    String perm;
                    if(i == parts.length-1) {
                        perm = node+part;
                    } else {
                        perm = node+part+".*";
                    }

                    perms2check.add(perm);
                }

            }

            List<String> finalPerms2Check = Lists.reverse(perms2check);
            finalPerms2Check.add(parts[0]+".*");
            finalPerms2Check.add("*");

            for (final String s : finalPerms2Check) {
                if(player.isPermissionSet(s) && player.hasPermission(s)) {
                    //System.out.println(s+": yes");
                    has = true;
                } else {
                    if(player.isPermissionSet(s) && !player.hasPermission(s)) {
                        //System.out.println(s+": no");
                        has = false;
                    }/*else{System.out.println(s+": idc");}*/
                }
            }

            return has;
        }

    }

    public static void main(String[] args) {
        String permission = "plots.admin.destroy.other";
        String[] parts = permission.split("\\.");
        boolean has = false;
        // match: *, plots.*, plots.admin.*, plots.admin.destroy.*, plots.admin.destroy.*
        // plots.admin.destroy.other
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];

            if(i == 0) {
                System.out.println("check: "+part+".*"+" ("+i+")");
            }

            if(i > 0) {
                String[] nodesBefore = Arrays.copyOfRange(parts, 0, i);
                ArrayList<String> nodes = new ArrayList<>();

                for (final String s : nodesBefore) {
                    nodes.add(s);
                }
                String node = "";
                for (final String nd : nodes) {
                    node = node + nd+".";
                }

                nodes.add(part);

                if(i == parts.length-1) {
                    System.out.println("check: "+node+part+" ("+i+")");
                } else {
                    System.out.println("check: "+node+part+".*"+" ("+i+")");
                }
            }
        }
    }
}
