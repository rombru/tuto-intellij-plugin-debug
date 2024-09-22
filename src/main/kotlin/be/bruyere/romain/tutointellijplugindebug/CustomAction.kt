package be.bruyere.romain.tutointellijplugindebug

import com.intellij.debugger.DebuggerManagerEx
import com.intellij.debugger.engine.DebuggerManagerThreadImpl
import com.intellij.debugger.engine.JavaValue
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.ui.Messages
import com.intellij.xdebugger.impl.ui.tree.actions.XDebuggerTreeActionBase
import com.jetbrains.jdi.ObjectReferenceImpl
import com.jetbrains.jdi.StringReferenceImpl
import com.sun.jdi.Method
import com.sun.jdi.Value

class CustomAction : AnAction(), DumbAware {

    override fun actionPerformed(event: AnActionEvent) {
        val methodName = "toString"
        val objectReference = getObjectReference(event)
        val managerThread = getManagerThread(event)

        if(objectReference != null && managerThread != null) {
            val method = getMethod(objectReference, methodName)
            val result = executeMethod(event, objectReference, managerThread, method)
            val stringValue = result as? StringReferenceImpl
            if(stringValue != null) {
                Messages.showMessageDialog(
                    stringValue.value(),
                    "It works!",
                    Messages.getInformationIcon())
            }
        }
    }

    /**
     * Executes a method in the debug session context for the current project on the target object.
     */
    private fun executeMethod(
        event: AnActionEvent,
        targetObjectRef: ObjectReferenceImpl,
        managerThread: DebuggerManagerThreadImpl,
        method: Method?
    ): Value? {
        val session = DebuggerManagerEx.getInstanceEx(event.project).context.debuggerSession
        val context = session?.process?.suspendManager?.pausedContext
        val command = CustomDebuggerCommandImpl(context, targetObjectRef, method)
        managerThread.invokeAndWait(command)
        return command.result
    }

    /**
     * Get the reference of the selected node in the debugger tree.
     */
    private fun getManagerThread(event: AnActionEvent): DebuggerManagerThreadImpl? {
        val node = XDebuggerTreeActionBase.getSelectedNode(event.dataContext)
        val valueContainer = node?.valueContainer
        val value = valueContainer as? JavaValue
        return value?.evaluationContext?.managerThread
    }

    /**
     * Get the reference of the method on the target object.
     */
    private fun getMethod(targetObjectRef: ObjectReferenceImpl, methodName: String): Method? =
        targetObjectRef.referenceType().methodsByName(methodName)[0]

    /**
     * Get the reference of the selected node in the debugger tree.
     */
    private fun getObjectReference(event: AnActionEvent): ObjectReferenceImpl? {
        val node = XDebuggerTreeActionBase.getSelectedNode(event.dataContext)
        val valueContainer = node?.valueContainer
        val value = valueContainer as? JavaValue
        return value?.descriptor?.value as ObjectReferenceImpl?
    }

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabledAndVisible = true
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}