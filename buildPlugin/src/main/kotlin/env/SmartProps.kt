package env

import java.util.Properties

class SmartProps(
    private val props: Properties?
) {
    fun takeConfig(systemEnv: String = "", propName: String = ""): String {
        return (System.getenv(systemEnv) ?: "").ifEmpty {
            props?.getProperty(propName, "") ?: ""
        }
    }
}
