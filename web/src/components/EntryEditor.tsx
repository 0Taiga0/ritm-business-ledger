import { ArrowDownLeft, ArrowUpRight, Trash2, X } from 'lucide-react';
import { useEffect, useRef, useState, type FormEvent } from 'react';
import { categories, isDate, parseAmount, type Entry, type EntryType } from '../domain/ledger';

interface Props {
  entry?: Entry;
  month: string;
  onSave: (entry: Entry) => void;
  onDelete: (id: string) => void;
  onClose: () => void;
}

export default function EntryEditor({ entry, month, onSave, onDelete, onClose }: Props) {
  const dialog = useRef<HTMLDialogElement>(null);
  const amountInput = useRef<HTMLInputElement>(null);
  const [type, setType] = useState<EntryType>(entry?.type ?? 'expense');
  const [amount, setAmount] = useState(entry ? String(entry.amountMinor / 100) : '');
  const [category, setCategory] = useState(entry?.category ?? 'services');
  const [date, setDate] = useState(entry?.date ?? `${month}-15`);
  const [counterparty, setCounterparty] = useState(entry?.counterparty ?? '');
  const [note, setNote] = useState(entry?.note ?? '');
  const [error, setError] = useState('');
  const [confirmDelete, setConfirmDelete] = useState(false);

  useEffect(() => {
    dialog.current?.showModal();
    amountInput.current?.focus();
  }, []);

  function switchType(next: EntryType) {
    setType(next);
    setCategory(next === 'income' ? 'projects' : 'services');
  }

  function submit(event: FormEvent) {
    event.preventDefault();
    const amountMinor = parseAmount(amount);
    if (!amountMinor)
      return setError('Введите положительную сумму, не больше двух знаков после запятой.');
    if (!isDate(date) || !counterparty.trim()) return setError('Укажите дату и контрагента.');
    try {
      onSave({
        id: entry?.id ?? crypto.randomUUID(),
        type,
        amountMinor,
        category,
        date,
        counterparty: counterparty.trim(),
        note: note.trim(),
      });
      onClose();
    } catch {
      setError(
        'Не удалось сохранить. Возможно, память браузера заполнена. Экспортируйте данные и попробуйте снова.',
      );
    }
  }

  return (
    <dialog ref={dialog} className="entry-dialog" onCancel={onClose} aria-labelledby="editor-title">
      <form onSubmit={submit}>
        <div className="dialog-heading">
          <div>
            <span className="eyebrow">УЧЕТ ОПЕРАЦИЙ</span>
            <h2 id="editor-title">{entry ? 'Детали операции' : 'Новая операция'}</h2>
          </div>
          <button type="button" className="icon-button" aria-label="Закрыть" onClick={onClose}>
            <X />
          </button>
        </div>
        <div className="type-toggle">
          <button
            type="button"
            className={type === 'income' ? 'selected' : ''}
            onClick={() => switchType('income')}
          >
            <ArrowDownLeft size={18} />
            Доход
          </button>
          <button
            type="button"
            className={type === 'expense' ? 'selected' : ''}
            onClick={() => switchType('expense')}
          >
            <ArrowUpRight size={18} />
            Расход
          </button>
        </div>
        <label>
          Сумма, ₽
          <input
            className="amount-input"
            ref={amountInput}
            inputMode="decimal"
            value={amount}
            onChange={(event) => setAmount(event.target.value)}
            placeholder="0"
            required
          />
        </label>
        <div className="field-pair">
          <label>
            Дата
            <input
              type="date"
              value={date}
              onChange={(event) => setDate(event.target.value)}
              required
            />
          </label>
          <label>
            Категория
            <select value={category} onChange={(event) => setCategory(event.target.value)}>
              {categories
                .filter((item) => item.type === type)
                .map((item) => (
                  <option key={item.id} value={item.id}>
                    {item.label}
                  </option>
                ))}
            </select>
          </label>
        </div>
        <label>
          Контрагент
          <input
            maxLength={120}
            value={counterparty}
            onChange={(event) => setCounterparty(event.target.value)}
            placeholder="Например, Студия Север"
            required
          />
        </label>
        <label>
          Комментарий
          <textarea
            maxLength={500}
            rows={2}
            value={note}
            onChange={(event) => setNote(event.target.value)}
            placeholder="За что прошла оплата"
          />
        </label>
        {error && (
          <p className="form-error" role="alert">
            {error}
          </p>
        )}
        <div className="dialog-actions">
          {entry && (
            <button type="button" className="danger-button" onClick={() => setConfirmDelete(true)}>
              <Trash2 size={17} />
              Удалить
            </button>
          )}
          <button type="submit" className="primary-button">
            {entry ? 'Сохранить изменения' : 'Добавить операцию'}
          </button>
        </div>
        {confirmDelete && (
          <div className="delete-confirm">
            <p>Удалить операцию? Отменить это действие нельзя.</p>
            <button type="button" onClick={() => setConfirmDelete(false)}>
              Оставить
            </button>
            <button
              type="button"
              className="danger-button"
              onClick={() => {
                try {
                  onDelete(entry!.id);
                  onClose();
                } catch {
                  setError('Не удалось удалить операцию.');
                }
              }}
            >
              Удалить операцию
            </button>
          </div>
        )}
      </form>
    </dialog>
  );
}
