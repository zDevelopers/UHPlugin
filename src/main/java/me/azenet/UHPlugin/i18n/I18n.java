package me.azenet.UHPlugin.i18n;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

/**
 * This class is used to manage the internationalization of this plugin.
 * It is plugin-independent.
 * 
 * @author Amaury Carrade
 * @version 1.0
 * @licence Mozilla Public Licence
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
	 * About the storage: 
	 *  - the translation files are stored in the directory {PluginDir}/i18n/{languageCode}.yml
	 *  - an additional file, {PluginDir}/i18n/manifest.yml, contains two keys. 
	 *     - "languages": this key is a list of the available languages.
	 *     - "version": this key is the version of the plugin (used to update the language files on the disk).
	 *       This needs to be the exact version defined in the plugin.yml file.
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
			p.getLogger().severe("Unable to load a malformed manifest");
			e.printStackTrace();
			return;
		}
		
		// The language files needs to be written
		// and to be overwritten if the plugin was updated (== different version)
		writeFilesIfNeeded();
		
		this.selectedLanguage = selectedLanguage;
		this.defaultLanguage = defaultLanguage;
		
		try {
			this.reloadLanguageFile(defaultLanguage, false);
			this.reloadLanguageFile(selectedLanguage, false);
		} catch (InvalidConfigurationException e) {
			p.getLogger().severe("Unable to load malformed language files");
			e.printStackTrace();
			return;
		}
	}
	
	/**
	 * Constructor.
	 * With this constructor, the default language is the english one (en_US).
	 * 
	 * @param plugin The plugin.
	 * @param selectedLanguage The selected language.
	 */
	public I18n(Plugin plugin, String selectedLanguage) {
		this(plugin, selectedLanguage, "en_US");
	}
	
	/**
	 * Constructor.
	 * With this constructor, the default language is the english one (en_US), and 
	 * the selected language is the one returned by Locale.getDefault().
	 * 
	 * @param plugin The plugin.
	 */
	public I18n(Plugin plugin) {
		this(plugin, Locale.getDefault().toString());
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
	 * Return the raw translation stored in the language file.
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
	 * Replace standard keys in the message, like {gold} for the gold color code.
	 * TODO
	 * 
	 * @param text
	 * @return
	 */
	private String replaceStandardKeys(String text) {
		return text;
	}
	
	/**
	 * (Re)loads a language file.
	 * 
	 * @param lang The language to (re)load.
	 * @param write If true the file will be written to the disk.
	 * @throws IllegalArgumentException if the language file does not exists for the given language.
	 */
	private void reloadLanguageFile(String lang, boolean write) throws InvalidConfigurationException {
		lang = this.cleanLanguageName(lang);
		
		if(!this.manifest.getList("languages").contains(lang)) { // Unknown language
			throw new IllegalArgumentException("The language " + lang + " is not registered");
		}
		
		if(this.languageFile.get(lang) == null) {
			this.languageFile.put(lang, new File(p.getDataFolder() + "/" + getLanguageFilePath(lang)));
		}
		
		this.languageSource.put(lang, YamlConfiguration.loadConfiguration(this.languageFile.get(lang)));
		
		// Default config
		Reader defaultLanguageFile = new InputStreamReader(p.getResource(getLanguageFilePath(lang)));
		if(defaultLanguageFile != null) {
			YamlConfiguration defaultLanguageSource = YamlConfiguration.loadConfiguration(defaultLanguageFile);
			this.languageSource.get(lang).setDefaults(defaultLanguageSource);
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
	 * Write the language files and the manifest to the disk if needed.
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
				if(manifest.getString("version") != p.getDescription().getVersion()) {
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
						reloadLanguageFile((String) lang, true);
					}
					else {
						File testFile = new File(p.getDataFolder() + "/" + this.getLanguageFilePath((String) lang));
						if(!testFile.exists()) {
							reloadLanguageFile((String) lang, true);
						}
					}
				} catch(IllegalArgumentException e) {
					p.getLogger().severe("Unable to load the language file for " + lang + ": the file does not exists.");
				}
				catch(InvalidConfigurationException e) {
					p.getLogger().severe("Unable to load a malformed language file for " + lang);
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
		// Else, the manifest has already be written.
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
