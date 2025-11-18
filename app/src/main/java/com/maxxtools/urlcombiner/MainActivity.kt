package com.maxxtools.urlcombiner

import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var urlRepository: UrlRepository
    private lateinit var urlAdapter: UrlAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var instructionsHeader: LinearLayout
    private lateinit var instructionsBody: TextView
    private lateinit var instructionsArrow: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        urlRepository = UrlRepository(this)
        recyclerView = findViewById(R.id.recyclerView)
        fabAdd = findViewById(R.id.fabAdd)
        instructionsHeader = findViewById(R.id.instructionsHeader)
        instructionsBody = findViewById(R.id.instructionsBody)
        instructionsArrow = findViewById(R.id.instructionsArrow)

        setupRecyclerView()
        setupFab()
        setupInstructions()

        // Verarbeite einen eingehenden Share-Intent, wenn vorhanden
        if (intent?.action == Intent.ACTION_SEND && intent.type?.startsWith("text/") == true) {
            intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
                handleSharedUrl(it)
            }
        }
    }

    private fun setupRecyclerView() {
        urlAdapter = UrlAdapter(
            onEditClick = { itemToEdit -> showAddOrEditUrlDialog(itemToEdit) },
            onDeleteClick = { itemToDelete -> deleteUrlItem(itemToDelete) }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = urlAdapter
    }

    private fun setupFab() {
        fabAdd.setOnClickListener {
            showAddOrEditUrlDialog()
        }
    }

    private fun setupInstructions() {
        setTextWithIcons()
        instructionsHeader.setOnClickListener {
            val isVisible = instructionsBody.visibility == View.VISIBLE
            instructionsBody.visibility = if (isVisible) View.GONE else View.VISIBLE
            val rotation = if (isVisible) 0f else 180f
            ObjectAnimator.ofFloat(instructionsArrow, "rotation", rotation).start()
        }
    }

    private fun setTextWithIcons() {
        val text = getString(R.string.instructions_body)
        val spannable = SpannableStringBuilder(text)

        // Ersetze alle Platzhalter durch die Icons
        replacePlaceholderWithIcon(spannable, "[ICON_ADD]", R.drawable.ic_add)
        replacePlaceholderWithIcon(spannable, "[ICON_EDIT]", R.drawable.ic_edit1)
        replacePlaceholderWithIcon(spannable, "[ICON_DELETE]", R.drawable.ic_delete1)

        instructionsBody.text = spannable
    }

    private fun replacePlaceholderWithIcon(spannable: SpannableStringBuilder, placeholder: String, drawableId: Int) {
        val drawable = ContextCompat.getDrawable(this, drawableId)?.apply {
            // Größe des Icons an die Textgröße anpassen (wichtig!)
            val size = (instructionsBody.textSize * 1.2).toInt()
            setBounds(0, 0, size, size)
        }

        if (drawable != null) {
            var index = spannable.indexOf(placeholder)
            while (index > -1) {
                val span = ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM)
                spannable.setSpan(span, index, index + placeholder.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
                index = spannable.indexOf(placeholder, index + 1)
            }
        }
    }

    private fun handleSharedUrl(sharedUrl: String) {
        // Entferne URL-Parameter (alles nach dem ersten '?')
        val cleanedUrl = sharedUrl.split('?').first()
        val baseUrls = urlRepository.getUrls()

        when {
            baseUrls.isEmpty() -> {
                Toast.makeText(this, getString(R.string.no_base_urls_available), Toast.LENGTH_LONG).show()
                finish() // Schließe die transparente Activity, wenn keine URLs vorhanden sind
            }
            baseUrls.size == 1 -> {
                val combinedUrl = baseUrls.first().url + cleanedUrl
                openUrlInBrowser(combinedUrl)
            }
            else -> {
                showBaseUrlSelectionDialog(baseUrls, cleanedUrl)
            }
        }
    }

    private fun showBaseUrlSelectionDialog(baseUrls: List<BaseUrlItem>, sharedUrl: String) {
        val urlNames = baseUrls.map { it.name }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.select_base_url))
            .setItems(urlNames) { _, which ->
                val selectedBaseUrl = baseUrls[which]
                val combinedUrl = selectedBaseUrl.url + sharedUrl
                openUrlInBrowser(combinedUrl)
            }
            .setOnCancelListener { finish() } // Schließe, wenn der Benutzer abbricht
            .show()
    }

    private fun openUrlInBrowser(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
            Toast.makeText(this, getString(R.string.opening_url), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.could_not_open_url), Toast.LENGTH_LONG).show()
        } finally {
            finish() // Immer schließen, um nicht in der (transparenten) App hängen zu bleiben
        }
    }

    override fun onResume() {
        super.onResume()
        loadAndDisplayUrls()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val languageCode = when (item.itemId) {
            R.id.menu_language_de -> "de"
            R.id.menu_language_en -> "en"
            R.id.menu_language_fr -> "fr"
            R.id.menu_language_es -> "es"
            R.id.menu_language_zh -> "zh"
            R.id.menu_language_ja -> "ja"
            else -> null
        }

        languageCode?.let {
            setLocale(it)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setLocale(languageCode: String) {
        val appLocale = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    private fun loadAndDisplayUrls() {
        val urls = urlRepository.getUrls()
        urlAdapter.submitList(urls)
    }

    private fun deleteUrlItem(item: BaseUrlItem) {
        val currentList = urlRepository.getUrls().toMutableList()
        currentList.remove(item)
        urlRepository.saveUrls(currentList)
        loadAndDisplayUrls()
    }

    private fun showAddOrEditUrlDialog(itemToEdit: BaseUrlItem? = null) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_url, null)
        val editTextName = dialogView.findViewById<EditText>(R.id.editTextName)
        val editTextUrl = dialogView.findViewById<EditText>(R.id.editTextUrl)

        // Sichere Prüfung: Wenn itemToEdit nicht null ist, wird der Dialog zum Bearbeiten vorbereitet
        if (itemToEdit != null) {
            editTextName.setText(itemToEdit.name)
            editTextUrl.setText(itemToEdit.url)
        }

        AlertDialog.Builder(this)
            .setTitle(
                if (itemToEdit != null) getString(R.string.edit_base_url) else getString(R.string.add_new_base_url_title)
            )
            .setView(dialogView)
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                val name = editTextName.text.toString().trim()
                val url = editTextUrl.text.toString().trim()

                if (name.isNotEmpty() && url.isNotEmpty()) {
                    val currentList = urlRepository.getUrls().toMutableList()
                    // Wenn itemToEdit nicht null ist, aktualisieren wir den Eintrag. Sonst fügen wir einen neuen hinzu.
                    if (itemToEdit != null) {
                        val index = currentList.indexOfFirst { it.id == itemToEdit.id }
                        if (index != -1) {
                            currentList[index] = itemToEdit.copy(name = name, url = url)
                        }
                    } else {
                        currentList.add(BaseUrlItem(name = name, url = url))
                    }
                    urlRepository.saveUrls(currentList)
                    loadAndDisplayUrls()
                } else {
                    Toast.makeText(this, getString(R.string.name_and_url_must_not_be_empty), Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .create()
            .show()
    }
}
