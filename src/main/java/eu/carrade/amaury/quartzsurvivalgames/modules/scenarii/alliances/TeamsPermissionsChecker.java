/*
 * Plugin UHCReloaded : Alliances
 *
 * Copyright ou © ou Copr. Amaury Carrade (2016)
 * Idées et réflexions : Alexandre Prokopowicz, Amaury Carrade, "Vayan".
 *
 * Ce logiciel est régi par la licence CeCILL soumise au droit français et
 * respectant les principes de diffusion des logiciels libres. Vous pouvez
 * utiliser, modifier et/ou redistribuer ce programme sous les conditions
 * de la licence CeCILL telle que diffusée par le CEA, le CNRS et l'INRIA
 * sur le site "http://www.cecill.info".
 *
 * En contrepartie de l'accessibilité au code source et des droits de copie,
 * de modification et de redistribution accordés par cette licence, il n'est
 * offert aux utilisateurs qu'une garantie limitée.  Pour les mêmes raisons,
 * seule une responsabilité restreinte pèse sur l'auteur du programme,  le
 * titulaire des droits patrimoniaux et les concédants successifs.
 *
 * A cet égard  l'attention de l'utilisateur est attirée sur les risques
 * associés au chargement,  à l'utilisation,  à la modification et/ou au
 * développement et à la reproduction du logiciel par l'utilisateur étant
 * donné sa spécificité de logiciel libre, qui peut le rendre complexe à
 * manipuler et qui le réserve donc à des développeurs et des professionnels
 * avertis possédant  des  connaissances  informatiques approfondies.  Les
 * utilisateurs sont donc invités à charger  et  tester  l'adéquation  du
 * logiciel à leurs besoins dans des conditions permettant d'assurer la
 * sécurité de leurs systèmes et ou de leurs données et, plus généralement,
 * à l'utiliser et l'exploiter dans les mêmes conditions de sécurité.
 *
 * Le fait que vous puissiez accéder à cet en-tête signifie que vous avez
 * pris connaissance de la licence CeCILL, et que vous en avez accepté les
 * termes.
 */

package eu.carrade.amaury.quartzsurvivalgames.modules.scenarii.alliances;

import fr.zcraft.quartzteams.QuartzTeamsPermission;
import fr.zcraft.quartzteams.permissions.PermissionsChecker;
import org.bukkit.permissions.Permissible;

public class TeamsPermissionsChecker implements PermissionsChecker {
    private final PermissionsChecker delegate;

    public TeamsPermissionsChecker(PermissionsChecker delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean hasPermission(Permissible permissible, QuartzTeamsPermission permission) {
        switch (permission) {

            case CREATE_TEAM:
            case DELETE_TEAM:
            case RESET_TEAMS:
            case LIST_TEAMS:  // Very important! Would allow to see all alliances.
            case JOIN_TEAM:
            case LEAVE_TEAM:
            case UPDATE_TEAMS_PLAYERS_LIST:
            case UPDATE_TEAM_COLOR:
            case UPDATE_OTHER_TEAM_NAME:
            case UPDATE_OTHER_TEAM_COLOR:
            case UPDATE_OTHER_TEAM_BANNER:
                return false;

            default:
                return delegate.hasPermission(permissible, permission);
        }
    }
}
