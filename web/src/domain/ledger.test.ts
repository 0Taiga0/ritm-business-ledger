import { describe, expect, it } from 'vitest';
import {
  balanceThrough,
  changePercent,
  exportCsv,
  importCsv,
  isDate,
  parseAmount,
  previousMonth,
  totals,
  validateEntries,
  type Entry,
} from './ledger';

const sale: Entry = {
  id: 'one',
  date: '2026-10-12',
  type: 'income',
  amountMinor: 125050,
  category: 'projects',
  counterparty: 'Кедр, студия',
  note: 'Первый этап\nОплачен',
};
const rent: Entry = {
  ...sale,
  id: 'two',
  type: 'expense',
  amountMinor: 25050,
  category: 'rent',
};

describe('ledger rules', () => {
  it('keeps money in integer kopecks', () => {
    expect(parseAmount('1 250,50')).toBe(125050);
    expect(parseAmount('0.01')).toBe(1);
    for (const invalid of ['0', '-5', '1.999', '1e4', 'NaN', ''])
      expect(parseAmount(invalid)).toBeNull();
    expect(totals([sale, rent])).toMatchObject({
      income: 125050,
      expense: 25050,
      profit: 100000,
    });
  });
  it('excludes future periods from balance and handles a year boundary', () => {
    expect(previousMonth('2026-01')).toBe('2025-12');
    expect(balanceThrough([sale, { ...rent, date: '2026-11-01' }], '2026-10')).toBe(28125050);
    expect(changePercent(100, 0)).toBeNull();
    expect(totals([]).margin).toBeNull();
  });
  it('rejects impossible dates, duplicate IDs and incompatible categories', () => {
    expect(isDate('2026-02-30')).toBe(false);
    expect(isDate('2024-02-29')).toBe(true);
    expect(() => validateEntries([sale, sale])).toThrow();
    expect(() => validateEntries([{ ...sale, category: 'rent' }])).toThrow();
  });
  it('round-trips Cyrillic, commas and newlines through CSV', () => {
    expect(importCsv(exportCsv([sale, rent]))).toEqual([sale, rent]);
  });
  it('escapes spreadsheet formula injection in exports', () => {
    expect(exportCsv([{ ...sale, counterparty: '=HYPERLINK("bad")' }])).toContain("'=HYPERLINK");
  });
});
