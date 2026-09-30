import { onBeforeUnmount, onMounted, type Ref } from 'vue'
import { onBeforeRouteLeave } from 'vue-router'

const LEAVE_MESSAGE = '这篇文章还有未保存的修改，确定离开吗？'

export function useUnsavedChanges(isDirty: Ref<boolean>) {
  const beforeUnload = (event: BeforeUnloadEvent) => {
    if (!isDirty.value) return
    event.preventDefault()
    event.returnValue = ''
  }

  onMounted(() => window.addEventListener('beforeunload', beforeUnload))
  onBeforeUnmount(() => window.removeEventListener('beforeunload', beforeUnload))

  onBeforeRouteLeave(() => !isDirty.value || window.confirm(LEAVE_MESSAGE))
}
