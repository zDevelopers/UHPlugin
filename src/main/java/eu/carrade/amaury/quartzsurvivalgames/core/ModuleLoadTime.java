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

package eu.carrade.amaury.quartzsurvivalgames.core;

import fr.zcraft.quartzlib.components.i18n.I;

public enum ModuleLoadTime {
    /**
     * Loads the module at startup, before the worlds are loaded.
     * <p>
     * Please note that most core modules (and localization) are not loaded at this point. Use that
     * for modules altering the world generation.
     */
    STARTUP(I.t("When the server starts")),

    /**
     * Loads the module after the world(s), or immediately if the plugin is reloaded.
     * The thing is, all worlds will be loaded when the module is.
     */
    POST_WORLD(I.t("When the worlds are loaded")),

    /**
     * Loads the module when the game phase is set to STARTING, i.e. when the /uh start command
     * is used.
     */
    ON_GAME_STARTING(I.t("When the game start command is executed")),

    /**
     * Loads the module when the game starts, i.e. when all players falls from their spawn into
     * the world.
     */
    ON_GAME_START(I.t("When the game starts for real (after teleportations)")),

    /**
     * Loads the module when the game ends.
     */
    ON_GAME_END(I.t("When the game ends"));


    private final String description;

    ModuleLoadTime(String description) {

        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
