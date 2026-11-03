import { ArrowDownLeft, ArrowUpRight, Ellipsis, Wallet } from 'lucide-react';
import { categoryFor, money, type Entry } from '../domain/ledger';

export default function TransactionTable({
  entries,
  onEdit,
}: {
  entries: Entry[];
  onEdit: (entry: Entry) => void;
}) {
  if (!entries.length)
    return (
      <div className="empty-state">
        <Wallet size={32} />
        <h3>Пока нет операций</h3>
        <p>Измените фильтры или добавьте первую запись.</p>
      </div>
    );
  return (
    <div className="table-scroll">
      <table>
        <thead>
          <tr>
            <th>Операция / контрагент</th>
            <th>Категория</th>
            <th>Дата</th>
            <th className="align-right">Сумма</th>
            <th>
              <span className="sr-only">Действия</span>
            </th>
          </tr>
        </thead>
        <tbody>
          {entries.map((entry) => (
            <tr key={entry.id}>
              <td>
                <button className="entry-title" onClick={() => onEdit(entry)}>
                  <span className={`entry-symbol ${entry.type}`}>
                    {entry.type === 'income' ? (
                      <ArrowDownLeft size={19} />
                    ) : (
                      <ArrowUpRight size={19} />
                    )}
                  </span>
                  <span>
                    <strong>{entry.counterparty}</strong>
                    <small>
                      {entry.note || (entry.type === 'income' ? 'Поступление' : 'Оплата')}
                    </small>
                  </span>
                </button>
              </td>
              <td>
                <span className={`category-pill ${entry.type}`}>
                  {categoryFor(entry.category).label}
                </span>
              </td>
              <td className="date-cell">
                {new Date(`${entry.date}T12:00:00`).toLocaleDateString('ru-RU', {
                  day: 'numeric',
                  month: 'short',
                })}
              </td>
              <td className={`align-right amount-cell ${entry.type}`}>
                {entry.type === 'income' ? '+' : '−'}
                {money(entry.amountMinor)}
              </td>
              <td>
                <button
                  className="icon-button row-menu"
                  aria-label={`Изменить ${entry.counterparty}`}
                  onClick={() => onEdit(entry)}
                >
                  <Ellipsis size={18} />
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
