package com.vexo.app

import data.model.DiaryEntry

sealed class DiaryListItem {
    data class Header(val title: String) : DiaryListItem()
    data class Entry(val diaryEntry: DiaryEntry) : DiaryListItem()
}