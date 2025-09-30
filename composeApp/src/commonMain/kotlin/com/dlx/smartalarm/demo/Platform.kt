package com.dlx.smartalarm.demo

interface Platform {
	val name: String
}

expect fun getPlatform(): Platform