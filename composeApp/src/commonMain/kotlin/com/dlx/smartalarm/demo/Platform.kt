package com.dlx.smartalarm.demo

interface Platform {
	val name: String
}

expect fun getPlatform(context: Any? = null): Platform
expect fun readTextFile(fileName: String): String?
expect fun writeTextFile(fileName: String, content: String)
expect fun getPlatformDataDirectory(): String