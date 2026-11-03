import { ArrowDownRight, ArrowUpRight, Minus } from 'lucide-react';
import { changePercent } from '../domain/ledger';

export default function ChangeBadge({
  current,
  previous,
  lowerIsBetter = false,
}: {
  current: number;
  previous: number;
  lowerIsBetter?: boolean;
}) {
  const value = changePercent(current, previous);
  const unfavorable = value !== null && (lowerIsBetter ? value > 0 : value < 0);
  const Icon = value === null || value === 0 ? Minus : value > 0 ? ArrowUpRight : ArrowDownRight;
  return (
    <span className={`change-badge ${unfavorable ? 'negative' : ''}`}>
      <Icon size={13} />
      {value === null ? 'нет базы' : `${value >= 0 ? '+' : ''}${value.toFixed(1)}%`}
    </span>
  );
}
