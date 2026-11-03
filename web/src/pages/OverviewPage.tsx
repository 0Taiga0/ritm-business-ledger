import { ArrowDownLeft, ArrowRight, ArrowUpRight, ChartNoAxesCombined, Wallet } from 'lucide-react';
import ChangeBadge from '../components/ChangeBadge';
import { CategoryChart, FlowChart } from '../components/Charts';
import TransactionTable from '../components/TransactionTable';
import {
  balanceThrough,
  budgetMinor,
  forMonth,
  money,
  previousMonth,
  totals,
  type Entry,
} from '../domain/ledger';

export default function OverviewPage({
  entries,
  month,
  onTransactions,
  onAnalytics,
  onEdit,
}: {
  entries: Entry[];
  month: string;
  onTransactions: () => void;
  onAnalytics: () => void;
  onEdit: (entry: Entry) => void;
}) {
  const selected = forMonth(entries, month);
  const current = totals(selected);
  const previous = totals(forMonth(entries, previousMonth(month)));
  const ordered = [...selected].sort(
    (a, b) => b.date.localeCompare(a.date) || a.id.localeCompare(b.id),
  );
  const budgetUsed = (current.expense / budgetMinor) * 100;
  return (
    <>
      <section className="metrics-grid" aria-label="Главные показатели">
        <article className="balance-card">
          <div className="card-label">
            Остаток на конец месяца
            <Wallet size={18} />
          </div>
          <strong className="metric-value">{money(balanceThrough(entries, month))}</strong>
          <div className="balance-footer">
            <span className="balance-dot" />
            Начальный остаток + все операции
          </div>
          <div className="balance-orbit" />
        </article>
        <article className="metric-card">
          <div className="card-label">
            Доходы
            <span className="metric-icon income">
              <ArrowDownLeft size={17} />
            </span>
          </div>
          <strong className="metric-value">{money(current.income)}</strong>
          <div className="metric-footer">
            <ChangeBadge current={current.income} previous={previous.income} />
            <span>к прошлому месяцу</span>
          </div>
        </article>
        <article className="metric-card">
          <div className="card-label">
            Расходы
            <span className="metric-icon expense">
              <ArrowUpRight size={17} />
            </span>
          </div>
          <strong className="metric-value">{money(current.expense)}</strong>
          <div className="metric-footer">
            <ChangeBadge current={current.expense} previous={previous.expense} lowerIsBetter />
            <span>к прошлому месяцу</span>
          </div>
        </article>
        <article className="metric-card profit-card">
          <div className="card-label">
            Денежный результат
            <ChartNoAxesCombined size={18} />
          </div>
          <strong className="metric-value">{money(current.profit)}</strong>
          <div className="metric-footer">
            <span className="margin-chip">
              {current.margin === null ? 'Нет доходов' : `${current.margin.toFixed(1)}% от доходов`}
            </span>
          </div>
        </article>
      </section>
      <div className="dashboard-grid">
        <section className="panel flow-panel">
          <div className="panel-heading">
            <div>
              <h2>Движение денег</h2>
              <p>Поступления и списания по дням</p>
            </div>
            <div className="chart-legend">
              <span>
                <i className="income-dot" />
                Доходы
              </span>
              <span>
                <i className="expense-dot" />
                Расходы
              </span>
            </div>
          </div>
          <FlowChart entries={selected} month={month} />
          <div className="flow-summary">
            <span>
              <i />
              {selected.filter((entry) => entry.type === 'income').length} поступлений за месяц
            </span>
            <strong>
              Результат <span>{money(current.profit)}</span>
            </strong>
          </div>
        </section>
        <section className="panel categories-panel">
          <div className="panel-heading">
            <div>
              <h2>Структура расходов</h2>
              <p>На что работает ваш бюджет</p>
            </div>
            <button
              className="icon-button"
              aria-label="Открыть аналитику"
              onClick={() => onAnalytics()}
            >
              <ArrowUpRight size={18} />
            </button>
          </div>
          <CategoryChart entries={selected} />
        </section>
      </div>
      <div className="lower-grid">
        <section className="panel transactions-panel">
          <div className="panel-heading">
            <div>
              <h2>
                Последние операции <span className="count-badge">{selected.length}</span>
              </h2>
              <p>Вся история поступлений и платежей</p>
            </div>
            <button className="text-button" onClick={() => onTransactions()}>
              Все операции
              <ArrowRight size={15} />
            </button>
          </div>
          <TransactionTable entries={ordered.slice(0, 4)} onEdit={onEdit} />
        </section>
        <section className="budget-card">
          <div className="budget-top">
            <span>Расходный план</span>
            <span>МЕСЯЦ</span>
          </div>
          <h3>
            Планируйте.
            <br />И двигайтесь дальше.
          </h3>
          <p>Лимит на текущий месяц</p>
          <strong>{money(budgetMinor)}</strong>
          <div className="budget-track">
            <i style={{ width: `${Math.min(budgetUsed, 100)}%` }} />
          </div>
          <div className="budget-status">
            <span>Использовано {budgetUsed.toFixed(0)}%</span>
            <strong>
              {money(Math.abs(budgetMinor - current.expense), true)}{' '}
              {current.expense > budgetMinor ? 'сверх' : 'осталось'}
            </strong>
          </div>
        </section>
      </div>
    </>
  );
}
