/**
 *  Plugin UltraHardcore (UHPlugin)
 *  Copyright (C) 2013 azenet
 *  Copyright (C) 2014 Amaury Carrade
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

package me.azenet.UHPlugin.i18n;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import com.google.common.base.Charsets;

/**
 * This class is used to manage the internationalization of this plugin.
 * It is plugin-independent.
 * 
 * @author Amaury Carrade
 * @version 1.2
 * @license Mozilla Public License
 *
 */
public class I18n {
	
	private Plugin p = null;
	
	private String selectedLanguage = null;
	private String defaultLanguage = null;
	
	private Map<String,FileConfiguration> languageSource = new HashMap<String,FileConfiguration>();
	private Map<String,File> languageFile = new HashMap<String,File>();
	private File manifestFile = null;
	private FileConfiguration manifest = null;
	
	/**
	 * Constructor.
	 * 
	 * About the storage: 
	 *  - the translation files are stored in the directory {PluginDir}/i18n/{languageCode}.yml
	 *  - an additional file, {PluginDir}/i18n/manifest.yml, contains two keys. 
	 *     - "languages": this key is a list of the available languages.
	 *     - "version": this key is the version of the plugin (used to update the language files on the disk).
	 *       This needs to be the exact version defined in the plugin.yml file.
	 * 
	 * Currently, an "en_US" language file is needed.
	 * Else, if the requested language is not available, a crash will occur when trying to load the en_US language.
	 * 
	 * @param plugin The plugin.
	 * @param selectedLanguage The selected language.
	 * @param defaultLanguage The default language, used when a key is missing or null.
	 */
	public I18n(Plugin plugin, String selectedLanguage, String defaultLanguage) {
		this.p = plugin;
		
		try {
			reloadManifest(false);
		} catch (InvalidConfigurationException e) {
			p.getLogger().log(Level.SEVERE, "Unable to load a malformed i18n manifest", e);
			return;
		}
		
		// The language files needs to be written
		// and to be overwritten if the plugin was updated (== different version)
		writeFilesIfNeeded();
		
		this.selectedLanguage = selectedLanguage;
		this.defaultLanguage = defaultLanguage;
		
		if(!isLanguageAvailable(selectedLanguage)) {
			if(isLanguageAvailable(getLanguageName(Locale.getDefault()))) {
				this.selectedLanguage = getLanguageName(Locale.getDefault());
			}
			else {
				this.selectedLanguage = "en_US";
			}
			p.getLogger().info("The selected language (" + selectedLanguage + ") is not available or not registered in the manifest; using " + this.selectedLanguage + ".");
		}
		if(!isLanguageAvailable(defaultLanguage)) {
			this.defaultLanguage  = "en_US";
			p.getLogger().info("The default language (" + defaultLanguage + ") is not available or not registered in the manifest; using en_US.");
		}
		
		try {
			this.reloadLanguageFile(this.selectedLanguage, false, false);
			this.reloadLanguageFile(this.defaultLanguage, false, false);
		} catch (InvalidConfigurationException e) {
			p.getLogger().log(Level.SEVERE, "Unable to load malformed language files (" + this.selectedLanguage + " or " + this.defaultLanguage + ")", e);
			return;
		}
	}
	
	/**
	 * Constructor.
	 * With this constructor, the default language is the English one (en_US).
	 * 
	 * @param plugin The plugin.
	 * @param selectedLanguage The selected language.
	 */
	public I18n(Plugin plugin, String selectedLanguage) {
		this(plugin, selectedLanguage, "en_US");
	}
	
	/**
	 * Constructor.
	 * With this constructor, the default language is the English one (en_US), and 
	 * the selected language is the one returned by Locale.getDefault().
	 * 
	 * @param plugin The plugin.
	 */
	public I18n(Plugin plugin) {
		this(plugin, getLanguageName(Locale.getDefault()));
	}
	
	/**
	 * Returns the translator of the given language.
	 * 
	 * @param lang The language.
	 * @return The translator, or null if not defined.
	 */
	public String getTranslator(String lang) {
		if(this.languageSource.get(lang) == null) {
			return null;
		}
		return this.languageSource.get(lang).getString("author");
	}
	
	/**
	 * Returns the selected language.
	 * 
	 * @return The code of the language (ex. en_US).
	 */
	public String getSelectedLanguage() {
		return this.selectedLanguage;
	}
	
	/**
	 * Returns the default (fallback) language.
	 * 
	 * @return The code of the language (ex. en_US).
	 */
	public String getDefaultLanguage() {
		return this.defaultLanguage;
	}
	
	/**
	 * Returns the translated value associated to a key.
	 * 
	 * @param key The key.
	 * @return
	 */
	public String t(String key) {
		// 1) main language available?
		if(this.getRawString(key, selectedLanguage) != null) {
			return this.replaceStandardKeys(this.getRawString(key, selectedLanguage));
		}
		// 2) default language maybe?
		else if(this.getRawString(key, defaultLanguage) != null) {
			return this.replaceStandardKeys(this.getRawString(key, defaultLanguage));
		}
		// 3) Nothing? Returns the key.
		else {
			return key;
		}
	}
	
	/**
	 * Returns the translated value associated to a key.
	 * Replaces {0}, {1}, etc. with the other parameters, following the order of these parameters.
	 * 
	 * @param key The key.
	 * @param params The additional parameters.
	 * @return
	 */
	public String t(String key, String... params) {
		int i = 0;
		String text = t(key);
		
		for(String param : params) {
			text = text.replace("{" + i + "}", param);
			i++;
		}
		
		return text;
	}
	
	/**
	 * Returns the raw translation stored in the language file.
	 * 
	 * @param key
	 * @param lang
	 * @return
	 */
	private String getRawString(String key, String lang) {
		if(this.languageSource.get(lang) == null) {
			return null;
		}
		return this.languageSource.get(lang).getString("keys." + key);
	}
	
	/**
	 * Replaces standard keys in the message, like {gold} for the gold color code.
	 * 
	 * @param text
	 * @return
	 */
	private String replaceStandardKeys(String text) {
		
		return text.replace("{black}", ChatColor.BLACK.toString())
					.replace("{darkblue}", ChatColor.DARK_BLUE.toString())
					.replace("{darkgreen}", ChatColor.DARK_GREEN.toString())
					.replace("{darkaqua}", ChatColor.DARK_AQUA.toString())
					.replace("{darkred}", ChatColor.DARK_RED.toString())
					.replace("{darkpurple}", ChatColor.DARK_PURPLE.toString())
					.replace("{gold}", ChatColor.GOLD.toString())
					.replace("{gray}", ChatColor.GRAY.toString())
					.replace("{darkgray}", ChatColor.DARK_GRAY.toString())
					.replace("{blue}", ChatColor.BLUE.toString())
					.replace("{green}", ChatColor.GREEN.toString())
					.replace("{aqua}", ChatColor.AQUA.toString())
					.replace("{red}", ChatColor.RED.toString())
					.replace("{lightpurple}", ChatColor.LIGHT_PURPLE.toString())
					.replace("{yellow}", ChatColor.YELLOW.toString())
					.replace("{white}", ChatColor.WHITE.toString())
					
					.replace("{bold}", ChatColor.BOLD.toString())
					.replace("{strikethrough}", ChatColor.STRIKETHROUGH.toString())
					.replace("{underline}", ChatColor.UNDERLINE.toString())
					.replace("{italic}", ChatColor.ITALIC.toString())
					.replace("{obfuscated}", ChatColor.MAGIC.toString())
					
					.replace("{reset}", ChatColor.RESET.toString())
					
					.replace("{ce}", ChatColor.RED.toString()) // error
					.replace("{cc}", ChatColor.GOLD.toString()) // command
					.replace("{ci}", ChatColor.WHITE.toString()) // info
					.replace("{cs}", ChatColor.GREEN.toString()) // success
					.replace("{cst}", ChatColor.DARK_GRAY.toString()); // status
	}
	
	/**
	 * Returns the name of the language associated with the given locale.
	 * <p>
	 * <code>locale.toString()</code> is not use to avoid a longer name, because the format
	 * of this method is:
	 * 
	 * <pre>language + "_" + country + "_" + (variant + "_#" | "#") + script + "-" + extensions</pre>
	 * 
	 * <p>
	 * Static because I need this in a constructor.
	 * 
	 * @param locale The locale
	 * @return The name of the locale, formatted following this scheme: "language_COUNTRY".
	 */
	private static String getLanguageName(Locale locale) {
		return locale.getLanguage() + "_" + locale.getCountry();
	}
	
	/**
	 * Returns true if the given language is available, using the manifest.
	 * 
	 * @param lang The lang.
	 * @return true if the given language is available.
	 */
	private boolean isLanguageAvailable(String lang) {
		return this.manifest.getList("languages").contains(lang);
	}
	
	/**
	 * (Re)loads a language file.
	 * 
	 * @param lang The language to (re)load.
	 * @param write If true the file will be written to the disk.
	 * @param writeOnly If true, the language file will not be kept in memory.
	 * @throws IllegalArgumentException if the language file does not exists for the given language.
	 * @throws InvalidConfigurationException if the language file is malformed (not a valid YML file).
	 */
	private void reloadLanguageFile(String lang, boolean write, boolean writeOnly) throws InvalidConfigurationException, IllegalArgumentException {
		lang = this.cleanLanguageName(lang);
		
		if(!isLanguageAvailable(lang)) { // Unknown language
			throw new IllegalArgumentException("The language " + lang + " is not registered");
		}
		
		if(this.languageFile.get(lang) == null) {
			this.languageFile.put(lang, new File(p.getDataFolder() + "/" + getLanguageFilePath(lang)));
		}
		 
		
		
		// The YAML configuration is loaded using a Reader, to specify the encoding to be used,
		// to be able to force UTF-8.
		// An InputStream of the language file is needed for this.
		InputStream languageFileInputStream = null;
		try {
			languageFileInputStream = languageFile.get(lang).toURI().toURL().openConnection().getInputStream();
			this.languageSource.put(lang, YamlConfiguration.loadConfiguration(new InputStreamReader(languageFileInputStream, Charsets.UTF_8)));
			
		} catch (FileNotFoundException e) {
			p.getLogger().log(Level.INFO, "Writing the language file for " + lang + "...");
			this.languageSource.put(lang, new YamlConfiguration());
			
		} catch (IOException e) {
			p.getLogger().log(Level.SEVERE, "Unable to load the language " + lang + ": input/output error. Please check if the file is readable.", e);
			return;
		}
		
		// Default config
		try {
			Reader defaultLanguageFile = new InputStreamReader(p.getResource(getLanguageFilePath(lang)), Charsets.UTF_8);
			if(defaultLanguageFile != null) {
				YamlConfiguration defaultLanguageSource = YamlConfiguration.loadConfiguration(defaultLanguageFile);
				this.languageSource.get(lang).setDefaults(defaultLanguageSource);
			}
		} catch(NullPointerException ignored) {
			// No "default" translation file available: user-only language file.
		}
		if(write) {
			try {
				if(!languageFile.get(lang).exists()) {
					p.saveResource(getLanguageFilePath(lang), false);
				}
				else {
					languageSource.get(lang).save(languageFile.get(lang));
				}
			} catch(IOException e) {
				p.getLogger().log(Level.SEVERE, "Unable to write the language file " + p.getDataFolder() + "/" + getLanguageFilePath(lang) + " to the disk, check the write permissions.", e);
			}
		}
		
		if(writeOnly) {
			languageSource.remove(lang);
			languageFile.remove(lang);
		}
	}
	
	/**
	 * Reloads the manifest.
	 * @return 
	 */
	private void reloadManifest(boolean write) throws InvalidConfigurationException {
		if(manifestFile == null) {
			manifestFile = new File(p.getDataFolder() + "/" + this.getLanguageFilePath("manifest"));
		}
		manifest = YamlConfiguration.loadConfiguration(manifestFile);
		
		Reader manifestReader = new InputStreamReader(p.getResource(getLanguageFilePath("manifest")));
		if(manifestReader != null) {
			YamlConfiguration manifestJar = YamlConfiguration.loadConfiguration(manifestReader);
			manifest.setDefaults(manifestJar);
		}
		
		if(write || !manifestFile.exists()) {
			try {
				if(!manifestFile.exists()) {
					p.saveResource(getLanguageFilePath("manifest"), false);
				}
				else {
					manifest.save(manifestFile);
				}
			} catch(IOException e) {
				p.getLogger().log(Level.SEVERE, "Unable to write the manifest file " + p.getDataFolder() + "/" + getLanguageFilePath("manifest") + " to the disk, check the write permissions.", e);
			}
		}
	}
	
	/**
	 * Writes the language files and the manifest to the disk if needed.
	 * The files are written if they are not already written of if the version changed.
	 */
	private void writeFilesIfNeeded() {
		// Files already written?
		File test = new File(p.getDataFolder() + "/" + this.getLanguageFilePath("manifest"));
		if(!test.exists()) { // Files not written
			writeLanguageFiles(false);
		}
		else {
			// Update needed?
			if(manifest != null) {
				if(!manifest.getString("version").equals(p.getDescription().getVersion())) {
					writeLanguageFiles(true);
				}
			}
		}
	}
	
	/**
	 * Writes the language files to the disk, on the {PluginFolder}/i18n/ directory.
	 * @param overwrite
	 */
	private void writeLanguageFiles(boolean overwrite) {
		// Normal language files
		for(Object lang : manifest.getList("languages")) {
			if(lang != null && lang instanceof String && !((String) lang).isEmpty()) {
				try {
					if(overwrite) {
						reloadLanguageFile((String) lang, true, true);
					}
					else {
						File testFile = new File(p.getDataFolder() + "/" + this.getLanguageFilePath((String) lang));
						if(!testFile.exists()) {
							reloadLanguageFile((String) lang, true, true);
						}
					}
				} catch(IllegalArgumentException e) {
					p.getLogger().severe("Unable to load the language file for " + lang + ": the file does not exists.");
				}
				catch(InvalidConfigurationException e) {
					p.getLogger().log(Level.SEVERE, "Unable to load a malformed language file for " + lang, e);
				}
			}
		}
		
		// Manifest
		if(overwrite) {
			try {
				reloadManifest(true);
			} catch (InvalidConfigurationException e) {
				p.getLogger().severe("Unable to load a malformed manifest.");
			}
		}
		// Else, the manifest has already been written.
	}
	
	/**
	 * Returns a cleaned version of a language name.
	 * 
	 * @param lang
	 * @return
	 */
	private String cleanLanguageName(String lang) {
		return lang.replace(' ', '_');
	}
	
	/**
	 * Returns the location of the language file inside the data folder, for the given language.
	 * 
	 * @param lang
	 * @return
	 */
	private String getLanguageFilePath(String lang) {
		return "i18n/" + lang + ".yml";
	}
}
