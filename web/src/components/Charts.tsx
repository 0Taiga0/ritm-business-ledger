import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import { cashFlow, expenseGroups, money, type Entry } from '../domain/ledger';

export function FlowChart({ entries, month }: { entries: Entry[]; month: string }) {
  return (
    <div
      className="flow-chart"
      role="img"
      aria-label="График доходов и расходов по дням выбранного месяца"
    >
      <ResponsiveContainer width="100%" height="100%">
        <BarChart
          data={cashFlow(entries, month)}
          barGap={1}
          margin={{ top: 20, right: 4, bottom: 0, left: -18 }}
        >
          <CartesianGrid vertical={false} stroke="#edf0ee" />
          <XAxis
            dataKey="day"
            tickLine={false}
            axisLine={false}
            tick={{ fill: '#8b9695', fontSize: 11 }}
            interval={4}
            tickFormatter={(value: number) => `${value}`}
          />
          <YAxis
            domain={[0, (maximum: number) => Math.max(maximum, 1000)]}
            tickLine={false}
            axisLine={false}
            tick={{ fill: '#8b9695', fontSize: 11 }}
            tickFormatter={(value: number) => `${value / 1000}к`}
          />
          <Tooltip
            cursor={{ fill: '#f2f6f3' }}
            formatter={(value) => money(Number(value) * 100)}
            labelFormatter={(day) => `${day} число`}
            contentStyle={{
              border: '1px solid #e4eae7',
              borderRadius: 12,
              fontSize: 12,
            }}
          />
          <Bar
            dataKey="income"
            name="Доходы"
            fill="#4c9a81"
            radius={[4, 4, 0, 0]}
            maxBarSize={12}
          />
          <Bar
            dataKey="expense"
            name="Расходы"
            fill="#d9bba1"
            radius={[4, 4, 0, 0]}
            maxBarSize={12}
          />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}

export function CategoryChart({ entries }: { entries: Entry[] }) {
  const groups = expenseGroups(entries);
  const total = groups.reduce((sum, group) => sum + group.amount, 0);
  return (
    <div className="category-chart">
      <div className="donut-wrap">
        <ResponsiveContainer width="100%" height="100%">
          <PieChart>
            <Pie
              data={groups}
              dataKey="amount"
              innerRadius={65}
              outerRadius={83}
              paddingAngle={3}
              stroke="none"
              cornerRadius={3}
            >
              {groups.map((group) => (
                <Cell key={group.id} fill={group.color} />
              ))}
            </Pie>
          </PieChart>
        </ResponsiveContainer>
        <div className="donut-center">
          <span>Всего расходов</span>
          <strong>{money(total, true)}</strong>
        </div>
      </div>
      <div className="category-legend">
        {groups.map((group) => (
          <div className="category-line" key={group.id}>
            <span>
              <i style={{ background: group.color }} />
              {group.label}
            </span>
            <strong>{total ? Math.round((group.amount / total) * 100) : 0}%</strong>
          </div>
        ))}
      </div>
    </div>
  );
}
