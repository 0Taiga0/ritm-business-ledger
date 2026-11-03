import Papa from 'papaparse';

export type EntryType = 'income' | 'expense';

export interface Entry {
  id: string;
  date: string;
  type: EntryType;
  amountMinor: number;
  category: string;
  counterparty: string;
  note: string;
}

export const categories = [
  { id: 'projects', label: 'Проекты', type: 'income', color: '#48a88c' },
  { id: 'support', label: 'Сопровождение', type: 'income', color: '#8cc3b5' },
  { id: 'team', label: 'Команда', type: 'expense', color: '#497f73' },
  { id: 'rent', label: 'Аренда', type: 'expense', color: '#8db9ab' },
  { id: 'marketing', label: 'Маркетинг', type: 'expense', color: '#d6b690' },
  { id: 'services', label: 'Сервисы', type: 'expense', color: '#a6afc8' },
  { id: 'tax', label: 'Налоги', type: 'expense', color: '#cc9178' },
  { id: 'other', label: 'Прочее', type: 'expense', color: '#bbc4c1' },
] as const;

export const openingBalanceMinor = 280_000_00;
export const budgetMinor = 500_000_00;

export function categoryFor(id: string) {
  return categories.find((category) => category.id === id) ?? categories[7];
}

export function money(amountMinor: number, compact = false): string {
  return new Intl.NumberFormat('ru-RU', {
    style: 'currency',
    currency: 'RUB',
    maximumFractionDigits: amountMinor % 100 === 0 ? 0 : 2,
    ...(compact ? { notation: 'compact', maximumFractionDigits: 1 } : {}),
  }).format(amountMinor / 100);
}

export function parseAmount(value: string): number | null {
  const cleaned = value.trim().replace(/\s/g, '').replace(',', '.');
  if (!/^\d{1,9}(\.\d{1,2})?$/.test(cleaned)) return null;
  const [rubles, kopecks = ''] = cleaned.split('.');
  const amount = Number(rubles) * 100 + Number(kopecks.padEnd(2, '0'));
  return amount > 0 && Number.isSafeInteger(amount) ? amount : null;
}

export function isDate(value: unknown): value is string {
  if (typeof value !== 'string' || !/^\d{4}-\d{2}-\d{2}$/.test(value)) return false;
  const parsed = new Date(`${value}T12:00:00Z`);
  return !Number.isNaN(parsed.valueOf()) && parsed.toISOString().slice(0, 10) === value;
}

export function validateEntries(value: unknown): Entry[] {
  if (!Array.isArray(value) || value.length > 50_000)
    throw new Error('Ожидается список до 50 000 операций.');
  const ids = new Set<string>();
  return value.map((item: unknown, index) => {
    const entry = item as Entry;
    const category = categories.find((candidate) => candidate.id === entry?.category);
    if (
      !entry ||
      typeof entry.id !== 'string' ||
      !entry.id ||
      ids.has(entry.id) ||
      !isDate(entry.date) ||
      !Number.isSafeInteger(entry.amountMinor) ||
      entry.amountMinor <= 0 ||
      entry.amountMinor > 99_999_999_999 ||
      !category ||
      category.type !== entry.type ||
      typeof entry.counterparty !== 'string' ||
      !entry.counterparty.trim() ||
      entry.counterparty.length > 120 ||
      typeof entry.note !== 'string' ||
      entry.note.length > 500
    ) {
      throw new Error(
        `Проверьте операцию в строке ${index + 2}: дата, сумма, категория или ID некорректны.`,
      );
    }
    ids.add(entry.id);
    return { ...entry };
  });
}

export function forMonth(entries: Entry[], month: string): Entry[] {
  return entries.filter((entry) => entry.date.startsWith(`${month}-`));
}

export function previousMonth(month: string): string {
  const [year, number] = month.split('-').map(Number);
  return new Date(Date.UTC(year, number - 2, 1)).toISOString().slice(0, 7);
}

export function totals(entries: Entry[]) {
  const income = entries
    .filter((entry) => entry.type === 'income')
    .reduce((sum, entry) => sum + entry.amountMinor, 0);
  const expense = entries
    .filter((entry) => entry.type === 'expense')
    .reduce((sum, entry) => sum + entry.amountMinor, 0);
  return {
    income,
    expense,
    profit: income - expense,
    margin: income ? ((income - expense) / income) * 100 : null,
  };
}

export function balanceThrough(entries: Entry[], month: string) {
  return (
    openingBalanceMinor + totals(entries.filter((entry) => entry.date.slice(0, 7) <= month)).profit
  );
}

export function changePercent(current: number, previous: number): number | null {
  return previous === 0 ? null : ((current - previous) / Math.abs(previous)) * 100;
}

export function monthLabel(month: string): string {
  return new Date(`${month}-01T12:00:00`)
    .toLocaleDateString('ru-RU', { month: 'long', year: 'numeric' })
    .replace(' г.', '');
}

export function cashFlow(entries: Entry[], month: string) {
  const [year, number] = month.split('-').map(Number);
  const days = new Date(year, number, 0).getDate();
  return Array.from({ length: days }, (_, index) => {
    const date = `${month}-${String(index + 1).padStart(2, '0')}`;
    const daily = totals(entries.filter((entry) => entry.date === date));
    return {
      day: index + 1,
      income: daily.income / 100,
      expense: daily.expense / 100,
    };
  });
}

export function expenseGroups(entries: Entry[]) {
  return categories
    .filter((category) => category.type === 'expense')
    .map((category) => ({
      ...category,
      amount: entries
        .filter((entry) => entry.category === category.id)
        .reduce((sum, entry) => sum + entry.amountMinor, 0),
    }))
    .filter((category) => category.amount > 0)
    .sort((a, b) => b.amount - a.amount);
}

export function exportCsv(entries: Entry[]): string {
  return '\uFEFF' + Papa.unparse(entries, { escapeFormulae: true });
}

export function importCsv(text: string): Entry[] {
  const parsed = Papa.parse<Record<string, string>>(text.replace(/^\uFEFF/, ''), {
    header: true,
    skipEmptyLines: true,
  });
  if (parsed.errors.length)
    throw new Error('Не удалось прочитать CSV. Проверьте разделители и кавычки.');
  return validateEntries(
    parsed.data.map((row) => ({
      ...row,
      amountMinor: Number(row.amountMinor),
    })),
  );
}
