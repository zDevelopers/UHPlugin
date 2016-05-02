/*
 * Copyright or © or Copr. Amaury Carrade (2014 - 2016)
 *
 * http://amaury.carrade.eu
 *
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-B
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 */
package eu.carrade.amaury.UHCReloaded;

import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.components.i18n.I18n;
import fr.zcraft.zlib.core.ZLib;
import org.bukkit.configuration.file.FileConfiguration;
import org.mockito.Matchers;
import org.powermock.api.mockito.PowerMockito;

import java.util.Locale;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class TestsUtils
{
    private static boolean staticPluginMocked = false;
    private static boolean i18nMocked = false;

    public static void mockI18n()
    {
        if (i18nMocked)
            return;

        PowerMockito.mockStatic(I18n.class);
        PowerMockito.mockStatic(I.class);

        when(I18n.getPrimaryLocale()).thenReturn(Locale.FRANCE);
        when(I18n.getFallbackLocale()).thenReturn(Locale.US);
        when(I18n.getLastTranslator(Matchers.any(Locale.class))).thenReturn("");
        when(I18n.getTranslationTeam(Matchers.any(Locale.class))).thenReturn("");
        when(I18n.getReportErrorsTo(Matchers.any(Locale.class))).thenReturn("");

        when(I.t(Matchers.anyString(), Matchers.any())).thenReturn("");
        when(I.tc(Matchers.anyString(), Matchers.anyString(), Matchers.any())).thenReturn("");
        when(I.tn(Matchers.anyString(), Matchers.anyString(), Matchers.anyInt(), Matchers.any())).thenReturn("");
        when(I.tcn(Matchers.anyString(), Matchers.anyString(), Matchers.anyString(), Matchers.anyInt(), Matchers.any())).thenReturn("");

        i18nMocked = true;
    }

    public static void mockStaticPlugin()
    {
        if (staticPluginMocked)
            return;

        final UHCReloaded pluginInstance = getMockedPluginInstance();

        PowerMockito.mockStatic(UHCReloaded.class);
        when(UHCReloaded.get()).thenReturn(pluginInstance);

        ZLib.init(pluginInstance);

        staticPluginMocked = true;
    }

    public static UHCReloaded getMockedPluginInstance()
    {
        final FileConfiguration config = mock(FileConfiguration.class);
        when(config.get(Matchers.anyString())).thenReturn(null);
        when(config.get(Matchers.anyString(), Matchers.anyObject())).thenReturn(null);
        when(config.getBoolean(Matchers.anyString())).thenReturn(true);
        when(config.getBoolean(Matchers.anyString(), Matchers.anyBoolean())).thenReturn(true);
        when(config.getInt(Matchers.anyString())).thenReturn(0);
        when(config.getInt(Matchers.anyString(), Matchers.anyInt())).thenReturn(0);
        when(config.getDouble(Matchers.anyString())).thenReturn(0d);
        when(config.getDouble(Matchers.anyString(), Matchers.anyDouble())).thenReturn(0d);
        when(config.getString(Matchers.anyString())).thenReturn("");
        when(config.getString(Matchers.anyString(), Matchers.anyString())).thenReturn("");
        when(config.getConfigurationSection(Matchers.anyString())).thenReturn(config);

        final UHCReloaded uhcReloaded = mock(UHCReloaded.class);
        when(uhcReloaded.getConfig()).thenReturn(config);

        return uhcReloaded;
    }
}
