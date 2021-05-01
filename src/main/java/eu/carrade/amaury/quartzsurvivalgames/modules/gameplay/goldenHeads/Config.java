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

package eu.carrade.amaury.quartzsurvivalgames.modules.gameplay.goldenHeads;

import static fr.zcraft.quartzlib.components.configuration.ConfigurationItem.item;
import static fr.zcraft.quartzlib.components.configuration.ConfigurationItem.section;

import fr.zcraft.quartzlib.components.configuration.ConfigurationInstance;
import fr.zcraft.quartzlib.components.configuration.ConfigurationItem;
import fr.zcraft.quartzlib.components.configuration.ConfigurationSection;
import java.io.File;

public class Config extends ConfigurationInstance {
    public static final ConfigurationItem<Boolean> DROP_HEAD_ON_DEATH = item("drop-head-on-death", true);
    public static final ConfigurationItem<Boolean> DROP_HEAD_ON_DEATH_PVP_ONLY =
            item("drop-head-on-death-pvp-only", false);
    public static final ConfigurationItem<Boolean> DISPLAY_REGEN_AMOUNT_ON_APPLES =
            item("display-regen-amount-on-apples", true);
    public static final GoldenAppleSection GOLDEN_APPLE = section("golden-apple", GoldenAppleSection.class);
    public static final EnchantedGoldenAppleSection ENCHANTED_GOLDEN_APPLE =
            section("enchanted-golden-apple", EnchantedGoldenAppleSection.class);
    public static final GoldenHeadSection PLAYER_GOLDEN_HEAD = section("player-golden-head", GoldenHeadSection.class);
    public static final GoldenHeadSection WITHER_GOLDEN_HEAD = section("wither-golden-head", GoldenHeadSection.class);
    public static final EnchantedGoldenHeadSection PLAYER_ENCHANTED_GOLDEN_HEAD =
            section("player-enchanted-golden-head", EnchantedGoldenHeadSection.class);
    public static final EnchantedGoldenHeadSection WITHER_ENCHANTED_GOLDEN_HEAD =
            section("wither-enchanted-golden-head", EnchantedGoldenHeadSection.class);
    public Config(File file) {
        super(file);
    }

    public static class GoldenAppleSection extends ConfigurationSection {
        public final ConfigurationItem<Boolean> ENABLE = item("enable", true);
        public final ConfigurationItem<Integer> REGENERATION = item("regeneration", 4);
    }

    public static class EnchantedGoldenAppleSection extends ConfigurationSection {
        public final ConfigurationItem<Boolean> ENABLE = item("enable", false);
        public final ConfigurationItem<Integer> REGENERATION = item("regeneration", 180);
    }

    public static class GoldenHeadSection extends GoldenAppleSection {
        public final ConfigurationItem<Integer> AMOUNT_CRAFTED = item("amount-crafted", 1);
        public final ConfigurationItem<Boolean> ADD_LORE = item("add-lore", true);
    }

    public static class EnchantedGoldenHeadSection extends EnchantedGoldenAppleSection {
        public final ConfigurationItem<Integer> AMOUNT_CRAFTED = item("amount-crafted", 1);
        public final ConfigurationItem<Boolean> ADD_LORE = item("add-lore", true);
    }
}
