import "./Pager.css";

interface PagerProps {
  page: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}

function Pager({ page, totalPages, onPageChange }: PagerProps) {
  if (totalPages <= 1) {
    return null;
  }

  return (
    <div className="pager">
      <button
        type="button"
        className="btn btn-outline-secondary btn-sm"
        disabled={page <= 0}
        onClick={() => onPageChange(page - 1)}
      >
        Previous
      </button>
      <span className="pager-status">
        Page {page + 1} of {totalPages}
      </span>
      <button
        type="button"
        className="btn btn-outline-secondary btn-sm"
        disabled={page >= totalPages - 1}
        onClick={() => onPageChange(page + 1)}
      >
        Next
      </button>
    </div>
  );
}

export default Pager;
