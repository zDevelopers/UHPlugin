/**
 *  Plugin UltraHardcore Reloaded (UHPlugin)
 *  Copyright (C) 2013 azenet
 *  Copyright (C) 2014-2015 Amaury Carrade
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see [http://www.gnu.org/licenses/].
 */
package me.azenet.UHPlugin;

import me.azenet.UHPlugin.i18n.I18n;
import org.mockito.Matchers;
import org.powermock.api.mockito.PowerMockito;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class TestsUtils {

	public static I18n getMockedI18n() {
		I18n mockedI18n = PowerMockito.mock(I18n.class);

		PowerMockito.when(mockedI18n.t(Matchers.anyString())).thenReturn("");

		return mockedI18n;
	}

	public static UHPlugin getMockedPluginInstance() {
		UHPlugin mockedPlugin = mock(UHPlugin.class);
		PowerMockito.mockStatic(UHPlugin.class);

		I18n i18n = getMockedI18n();

		when(mockedPlugin.getI18n()).thenReturn(i18n);
		when(UHPlugin.i()).thenReturn(i18n);
		
		return mockedPlugin;
	}

}
