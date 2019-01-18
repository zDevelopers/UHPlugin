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
package eu.carrade.amaury.UHCReloaded.modules.other.about;

import eu.carrade.amaury.UHCReloaded.utils.CommandUtils;
import eu.carrade.amaury.UHCReloaded.shortcuts.UR;
import fr.zcraft.zlib.components.commands.Command;
import fr.zcraft.zlib.components.commands.CommandInfo;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.components.i18n.I18n;


@CommandInfo (name = "about")
public class AboutCommand extends Command
{
    @Override
    protected void run()
    {
        final AboutModule about = UR.module(AboutModule.class);

        CommandUtils.displaySeparator(sender);

        info(I.t("{yellow}{0} - version {1}", about.getPluginName(), about.getVersion()) + " - " + about.getStability().toString().toLowerCase());
        info(I.t("Plugin made with love by {0}.", about.getFormattedAuthors()));

        if (about.getGitVersion() != null)
        {
            info(I.t("Build number: {0}.", about.getGitVersion()));
        }
        else
        {
            info(I.t("Build number not available."));
        }

        // Translation

        info("");
        info(I.t("{aqua}------ Translations ------"));
        info(I.t("Current language: {0} (translated by {1}).", I18n.getPrimaryLocale(), I18n.getTranslationTeam(I18n.getPrimaryLocale())));
        info(I.t("Fallback language: {0} (translated by {1}).", I18n.getFallbackLocale(), I18n.getTranslationTeam(I18n.getFallbackLocale())));

        info("");
        info(I.t("{aqua}------ License ------"));
        info(I.t("Published under the CeCILL-B License."));

        CommandUtils.displaySeparator(sender);
    }
}
