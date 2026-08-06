package com.vehiclemanager.data

import android.app.Application
import com.vehiclemanager.data.entity.Part
import kotlinx.coroutines.*

object SeedData {
    fun seedParts(dao: com.vehiclemanager.data.dao.PartDao) {
        val parts = listOf(
            Part(partName="机油", shop="铁马维修", qty=2, unitPrice=130, amount=260),
            Part(partName="机油", shop="洪亮价格", qty=2, unitPrice=120, amount=240),
            Part(partName="机滤", shop="铁马维修", qty=1, unitPrice=60, amount=60),
            Part(partName="机滤", shop="洪亮价格", qty=1, unitPrice=40, amount=40),
            Part(partName="空滤", shop="铁马维修", qty=1, unitPrice=90, amount=90),
            Part(partName="空滤", shop="洪亮价格", qty=1, unitPrice=80, amount=80),
            Part(partName="链条保养", shop="铁马维修", qty=1, unitPrice=190, amount=190),
            Part(partName="前刹车片", shop="铁马维修", qty=2, unitPrice=280, amount=560),
            Part(partName="前刹车片", shop="洪亮价格", qty=2, unitPrice=150, amount=300),
            Part(partName="后刹车片", shop="铁马维修", qty=1, unitPrice=280, amount=280),
            Part(partName="后刹车片", shop="洪亮价格", qty=1, unitPrice=150, amount=150),
            Part(partName="防冻液", shop="铁马维修", qty=1, unitPrice=80, amount=80),
            Part(partName="防冻液", shop="洪亮价格", qty=1, unitPrice=50, amount=50)
        )
        runBlocking { dao.insertAll(parts) }
    }
}
