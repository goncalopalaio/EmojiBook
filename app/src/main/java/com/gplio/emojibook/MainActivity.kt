package com.gplio.emojibook

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.sax.Element
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.list_item_emoji.view.*


class MainActivity : AppCompatActivity() {
    val list = ArrayList<ElementData>();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fillList()

        rv_emoji_list.layoutManager = GridLayoutManager(this, 4)
        rv_emoji_list.adapter = EmojiListAdapter(this, list)

    }

    private fun joinUtf(temporaryBuffer: StringBuffer, a: List<String>): String {
        temporaryBuffer.setLength(0)
        for (s in a) {
            temporaryBuffer.append(Character.toChars(Integer.parseInt(s.trim(), 16)))
        }

        return temporaryBuffer.toString()
    }

    private fun fillList() {
        val buffer = StringBuffer()

        // todo this should be moved to another thread
        val input = resources.openRawResource(R.raw.emoji_text)
        val reader = input.bufferedReader()

        var line : String? = ""
        while (line != null) {
            line = reader.readLine()
            if ("EMOJI".equals(line)) {

                val codesJoined = reader.readLine()
                val description = reader.readLine()
                val qualification = reader.readLine()

                Log.d("zzz", "Reading Emoji -> code_joined: " + codesJoined + " description: " + description + " qualification: " + qualification)

                val codes = codesJoined.split(",")
                val emoji = joinUtf(buffer, codes)
                val isFullyQualified = if ("true".equals(qualification)) true else false

                list.add(ElementData(ElementType.EMOJI, emoji, description, isFullyQualified))
            } else if ("SEPARATOR".equals(line)){
                val descriptionText = reader.readLine()
                Log.d("zzz", "Reading Separator -> " + descriptionText)
                list.add(ElementData(ElementType.SEPARATOR, descriptionText))
            }
        }

        // todo missing exclusions
        generateSection(buffer, "miscellaneousSymbols", list, 0x2600, 0x26ff)
        generateSection(buffer, "dingbats", list, 0x2700, 0x27bf)
        generateSection(buffer, "miscellaneousSymbolsAndPictographs", list, 0x1f300, 0x1f5ff)
        generateSection(buffer, "emoticons", list, 0x1f600, 0x1f64f)
        generateSection(buffer, "transportAndMapSymbols", list, 0x1f680, 0x1f6ff)
        generateSection(buffer, "supplementalSymbolsAndPictographs", list, 0x1f900, 0x1f9ff)
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun generateSection(temporaryBuffer: StringBuffer, separatorText: String, list : ArrayList<ElementData>, start: Int, end: Int) {
        list.add(ElementData(ElementType.SEPARATOR, separatorText))
        for (i in start..end) {
            temporaryBuffer.setLength(0)
            temporaryBuffer.append(Character.toChars(i))
            list.add(ElementData(ElementType.EMOJI, temporaryBuffer.toString()))
        }
    }

    class EmojiListAdapter (val context: Context, val items: ArrayList<ElementData>) : RecyclerView.Adapter<CustomViewHolder>() {
        override fun getItemCount(): Int {
            return items.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
            return CustomViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item_emoji, parent, false))
        }

        override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
            val item = this.items.get(position)
            when (item.type) {
                ElementType.EMOJI -> {
                    holder.tvEmoji.visibility = View.VISIBLE
                    holder.tvEmojiDescription.visibility = View.VISIBLE
                    holder.tvSeparator.visibility = View.GONE

                    holder.tvEmoji.text = item.text
                    holder.tvEmojiDescription.text = item.description
                }

                ElementType.SEPARATOR -> {
                    holder.tvEmoji.visibility = View.GONE
                    holder.tvEmojiDescription.visibility = View.GONE
                    holder.tvSeparator.visibility = View.VISIBLE
                    holder.tvSeparator.text = item.text
                }
            }
        }
    }

    class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var tvEmoji = view.tv_main_emoji
        var tvEmojiDescription = view.tv_main_emoji_description
        var tvSeparator = view.tv_separator_text
    }

    enum class ElementType {
        EMOJI, SEPARATOR
    }

    data class ElementData(var type: ElementType, var text: String, var description: String = "", var fullQualified: Boolean = true)
}
