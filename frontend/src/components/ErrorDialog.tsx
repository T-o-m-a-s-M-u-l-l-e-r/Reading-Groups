import { useEffect, useRef } from "react"
import "./ErrorDialog.css"

type ErrorDialogProps = {
  title: string,
  message: string,
  onClose: () => void
}

function ErrorDialog({ title, message, onClose }: ErrorDialogProps) {
  const dialog = useRef<HTMLDialogElement>(null)

  useEffect(() => {
    if (message) {
      dialog.current?.showModal()
    } else {
      dialog.current?.close()
    }
  }, [message])

  return (
    <dialog ref={dialog} className="error-dialog" aria-labelledby="error-dialog-title"
      aria-describedby="error-dialog-message" onClose={onClose}>
      <h2 id="error-dialog-title">{title}</h2>
      <p id="error-dialog-message">{message}</p>
      <button type="button" className="button button-primary" onClick={() => dialog.current?.close()}>Close</button>
    </dialog>
  )
}

export default ErrorDialog
