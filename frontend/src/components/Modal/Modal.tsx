import type { ReactNode } from "react";
import "./Modal.css";

interface Props {
  isOpen: boolean;
  onClose: () => void;
  children: ReactNode;
}

function Modal({ isOpen, onClose, children }: Props) {
  if (!isOpen) return null;

  return (
    <>
      <div className="modal-backdrop show" onClick={onClose} />
      <div className="modal show" style={{ display: "block" }} onClick={onClose}>
        <div className="modal-dialog" onClick={(e) => e.stopPropagation()}>
          <div className="modal-content">{children}</div>
        </div>
      </div>
    </>
  );
}

export default Modal;
