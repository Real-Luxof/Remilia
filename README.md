# Remilia

Patchouli-Hex Casting addon that adds a bunch of niceties for players as well as mod developers.  
WIP.  
- Server-client synced variables.
    - Used for mod config or patchouli config flag syncing.
    - Example use case is modifying a multiplier for some spell's cost if the server config says so.
- Macro injection into the Patchouli book.
- Ancient Scrolls (or even just Scrolls) can be right-clicked to learn their stroke order in your book. (damn Miyu took that one)
- Method-calling macros that give the given text as arguments to get back other text.
- $(^N) clears the (N+1)-to-last command to cause an effect that's still in effect. :x:
    - Nullifies it.
    - Works only with non-method-calling macros.
- Tooltips can have Patchouli formatting now.
- Hotkey (ctrl + alt, perchance) to turn patterns into raw anglesig and startdir (in chat). :x:
- Real-time macros (macros that update as the player is looking at them) :x:
- Customizeable Patchouli font. :x:
- Easily extendible Patchoul entries. :x:
    - players may write their own pages.
    - player-written stuff is stored in PersistentState.
- Per-world pattern shapes for everyone? :x:
- Better Patchouli search. :x:
    - Prefix, suffix, regex, or rubber band.
    - Search words, page titles, entries, or categories.
- Sort by mods. :x:
    - gray out categories/entries/pages of blacklisted mods
    - or only not gray out whitelisted mods.
    - probably also give +999 sort prio to grayed out mods (changeable in config)
- Hook to generate limitless categories, entries, or pages in a specific format. :x:
    - players may write their own categories and entries??
    - player-written stuff is stored in PersistentState.
- Easily extendible Patchoul entries. :x:
- Hook to generate limitless categories, entries, or pages in a specific format. :x:
- Markdown for Patchouli? :x:
