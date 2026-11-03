import ChangeBadge from '../components/ChangeBadge';
import { CategoryChart, FlowChart } from '../components/Charts';
import {
  expenseGroups,
  forMonth,
  money,
  previousMonth,
  totals,
  type Entry,
} from '../domain/ledger';

export default function AnalyticsPage({ entries, month }: { entries: Entry[]; month: string }) {
  const selected = forMonth(entries, month);
  const current = totals(selected);
  const previous = totals(forMonth(entries, previousMonth(month)));
  return (
    <>
      <div className="analytics-intro">
        <div>
          <span className="eyebrow">ДЕНЕЖНЫЙ РЕЗУЛЬТАТ</span>
          <h2>{money(current.profit)}</h2>
          <p>
            Доходы минус расходы за выбранный месяц.
            <br />
            Это движение денег, без учета начислений и обязательств.
          </p>
        </div>
        <div className="analytics-ratio">
          <span>Доля результата в доходах</span>
          <strong>
            {current.margin === null ? 'Нет данных' : `${current.margin.toFixed(1)}%`}
          </strong>
          <ChangeBadge current={current.profit} previous={previous.profit} />
        </div>
      </div>
      <div className="dashboard-grid">
        <section className="panel">
          <div className="panel-heading">
            <div>
              <h2>Динамика за месяц</h2>
              <p>Каждый столбец соответствует реальным операциям</p>
            </div>
          </div>
          <FlowChart entries={selected} month={month} />
        </section>
        <section className="panel">
          <div className="panel-heading">
            <div>
              <h2>Куда уходят деньги</h2>
              <p>{expenseGroups(selected).length} категорий расходов</p>
            </div>
          </div>
          <CategoryChart entries={selected} />
        </section>
      </div>
      <section className="panel breakdown">
        <div className="panel-heading">
          <div>
            <h2>Расходы по категориям</h2>
            <p>От большей доли к меньшей</p>
          </div>
        </div>
        {expenseGroups(selected).map((item) => (
          <div className="breakdown-row" key={item.id}>
            <span>
              <i style={{ background: item.color }} />
              {item.label}
            </span>
            <div className="breakdown-track">
              <i
                style={{
                  width: `${current.expense ? (item.amount / current.expense) * 100 : 0}%`,
                  background: item.color,
                }}
              />
            </div>
            <strong>{money(item.amount)}</strong>
            <small>
              {current.expense ? ((item.amount / current.expense) * 100).toFixed(1) : 0}%
            </small>
          </div>
        ))}
      </section>
    </>
  );
}
