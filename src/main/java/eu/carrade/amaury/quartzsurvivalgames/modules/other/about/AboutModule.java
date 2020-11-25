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
package eu.carrade.amaury.quartzsurvivalgames.modules.other.about;

import eu.carrade.amaury.quartzsurvivalgames.QuartzSurvivalGames;
import eu.carrade.amaury.quartzsurvivalgames.core.ModuleCategory;
import eu.carrade.amaury.quartzsurvivalgames.core.ModuleInfo;
import eu.carrade.amaury.quartzsurvivalgames.core.QSGModule;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.game.GameModule;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.game.GamePhase;
import eu.carrade.amaury.quartzsurvivalgames.modules.core.sidebar.SidebarInjector;
import eu.carrade.amaury.quartzsurvivalgames.shortcuts.QSG;
import fr.zcraft.quartzlib.components.commands.Command;
import fr.zcraft.quartzlib.components.i18n.I;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;


@ModuleInfo (
        name = "About",
        description = "Provides information about this plugin.",
        category = ModuleCategory.OTHER,
        icon = Material.BOOK
)
public class AboutModule extends QSGModule
{
    private String shortName = "Quartz Survival Games";
    private String version = null;
    private Stability stability = null;
    private String gitVersion = null;
    private String authors = null;

    @Override
    protected void onEnable()
    {
        computeVersion();
        computeGitVersion();
        computeFormattedAuthors();
    }

    @Override
    public List<Class<? extends Command>> getCommands()
    {
        return Collections.singletonList(AboutCommand.class);
    }

    @Override
    public void injectIntoSidebar(Player player, SidebarInjector injector)
    {
        if (QSG.module(GameModule.class).getPhase() == GamePhase.WAIT)
        {
            injector.injectLines(
                    SidebarInjector.SidebarPriority.VERY_BOTTOM,
                    true, false,
                    ChatColor.GRAY + shortName + " " + version
            );

            if (getVersion() != null)
            {
                injector.injectLines(
                        SidebarInjector.SidebarPriority.VERY_BOTTOM,
                        false,
                        ChatColor.GRAY + gitVersion.substring(0, 8)
                );
            }

            if (stability != Stability.STABLE)
            {
                switch (stability)
                {
                    case BETA:
                        injector.injectLines(
                                SidebarInjector.SidebarPriority.VERY_BOTTOM,
                                true,
                                I.t("{yellow}Beta version")
                        );
                        break;

                    case ALPHA:
                        injector.injectLines(
                                SidebarInjector.SidebarPriority.VERY_BOTTOM,
                                true,
                                I.t("{red}Development version")
                        );
                        break;
                }
            }
        }
    }

    public String getPluginName()
    {
        return QSG.get().getDescription().getDescription();
    }

    public String getShortPluginName()
    {
        return shortName;
    }

    public String getVersion()
    {
        return version;
    }

    public Stability getStability()
    {
        return stability;
    }

    public String getGitVersion()
    {
        return gitVersion;
    }

    public List<String> getAuthors()
    {
        return QSG.get().getDescription().getAuthors();
    }

    public String getFormattedAuthors()
    {
        return authors;
    }

    private void computeGitVersion()
    {
        try
        {
            final Class<? extends QuartzSurvivalGames> clazz = QSG.get().getClass();
            final String className = clazz.getSimpleName() + ".class";
            final String classPath = clazz.getResource(className).toString();

            if (classPath.startsWith("jar"))  // Class from JAR
            {
                final String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) +
                        "/META-INF/MANIFEST.MF";
                final Manifest manifest = new Manifest(new URL(manifestPath).openStream());
                final Attributes attr = manifest.getMainAttributes();

                gitVersion = attr.getValue("Git-Commit");
            }
        }
        catch (IOException e)
        {
            // Build not available.
        }
    }

    private void computeFormattedAuthors()
    {
        final StringBuilder authors = new StringBuilder();
        final List<String> listAuthors = getAuthors();

        for (final String author : listAuthors)
        {
            if (!author.equals(listAuthors.get(0)))
            {
                if (author.equals(listAuthors.get(listAuthors.size() - 1)))
                {
                    /// The "and" in the authors list (like "Amaury Carrade, azenet and João Roda")
                    authors.append(" ").append(I.tc("authors_list", "and")).append(" ");
                }
                else
                {
                    authors.append(", ");
                }
            }

            authors.append(author);
        }

        this.authors = authors.toString();
    }

    private void computeVersion()
    {
        final String[] versionParts = QSG.get().getDescription().getVersion().split("-", 2);

        version = versionParts[0];

        if (versionParts.length >= 2)
        {
            switch (versionParts[1].trim().toLowerCase())
            {
                case "dev":
                case "alpha":
                    stability = Stability.ALPHA;
                    break;

                case "beta":
                case "bêta":
                case "pre":
                case "prerelease":
                case "pre-release":
                    stability = Stability.BETA;
                    break;

                default:
                    stability = Stability.STABLE;
            }
        }

        else stability = Stability.STABLE;
    }

    public enum Stability
    {
        STABLE, BETA, ALPHA
    }
}
