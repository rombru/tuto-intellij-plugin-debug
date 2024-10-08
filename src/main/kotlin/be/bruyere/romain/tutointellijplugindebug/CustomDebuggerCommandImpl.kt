package be.bruyere.romain.tutointellijplugindebug

import com.intellij.debugger.engine.SuspendContextImpl
import com.intellij.debugger.engine.events.DebuggerCommandImpl
import com.jetbrains.jdi.ObjectReferenceImpl
import com.sun.jdi.Method
import com.sun.jdi.Value

/**
 * Command to execute a method on a target object during a debug session
 * The command implements DebuggerCommandImpl to be executable inside a manager thread
 */
class CustomDebuggerCommandImpl(
    private val context: SuspendContextImpl?,
    private val objectReference: ObjectReferenceImpl,
    private val method: Method?
) : DebuggerCommandImpl() {
    var result: Value? = null
        private set

    override fun action() {
        val threadReference = context?.frameProxy?.threadProxy()?.threadReference
        result = objectReference.invokeMethod(
            threadReference,
            method,
            listOf(),
            ObjectReferenceImpl.INVOKE_SINGLE_THREADED
        )
    }
}