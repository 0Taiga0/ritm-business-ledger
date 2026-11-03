import {
  ArrowUpRight,
  CalendarDays,
  ChartNoAxesCombined,
  Check,
  ChevronDown,
  CircleHelp,
  Download,
  LayoutDashboard,
  ListFilter,
  Menu,
  Plus,
  Search,
  Settings2,
  ShieldCheck,
  Wallet,
  X,
} from 'lucide-react';
import { useRef, useState, type ChangeEvent } from 'react';
import EntryEditor from './components/EntryEditor';
import TransactionTable from './components/TransactionTable';
import { saveFile } from './domain/download';
import {
  categories,
  exportCsv,
  forMonth,
  importCsv,
  money,
  monthLabel,
  totals,
  type Entry,
} from './domain/ledger';
import AnalyticsPage from './pages/AnalyticsPage';
import DataPage from './pages/DataPage';
import OverviewPage from './pages/OverviewPage';
import { useLedger } from './useLedger';

type Page = 'overview' | 'transactions' | 'analytics' | 'settings';
const navigation = [
  { id: 'overview', title: 'Обзор', Icon: LayoutDashboard },
  { id: 'transactions', title: 'Операции', Icon: Wallet },
  { id: 'analytics', title: 'Аналитика', Icon: ChartNoAxesCombined },
  { id: 'settings', title: 'Данные', Icon: Settings2 },
] as const;

export default function App() {
  const ledger = useLedger();
  const [page, setPage] = useState<Page>('overview');
  const [month, setMonth] = useState('2026-10');
  const [search, setSearch] = useState('');
  const [type, setType] = useState('all');
  const [category, setCategory] = useState('all');
  const [editor, setEditor] = useState<Entry | 'new' | null>(null);
  const [notice, setNotice] = useState('');
  const [mobileNav, setMobileNav] = useState(false);
  const importInput = useRef<HTMLInputElement>(null);
  const selected = forMonth(ledger.entries, month);
  const months = Array.from(
    new Set([
      '2026-10',
      new Date().toISOString().slice(0, 7),
      month,
      ...ledger.entries.map((entry) => entry.date.slice(0, 7)),
    ]),
  )
    .sort()
    .reverse();
  const ordered = [...selected].sort(
    (a, b) => b.date.localeCompare(a.date) || a.id.localeCompare(b.id),
  );
  const filtered = ordered.filter(
    (entry) =>
      (type === 'all' || entry.type === type) &&
      (category === 'all' || entry.category === category) &&
      `${entry.counterparty} ${entry.note}`.toLowerCase().includes(search.toLowerCase()),
  );

  function notify(text: string) {
    setNotice(text);
  }

  function exportMonth() {
    saveFile(exportCsv(selected), `ritm-${month}.csv`, 'text/csv;charset=utf-8');
    notify('CSV с операциями за выбранный месяц сохранен.');
  }

  async function handleImport(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0];
    if (!file) return;
    try {
      if (file.size > 10_000_000) throw new Error('Максимальный размер файла 10 МБ.');
      const imported = importCsv(await file.text());
      const count = ledger.merge(imported);
      notify(`Импортировано операций: ${count}. Совпадающие ID пропущены.`);
    } catch (error) {
      notify(error instanceof Error ? error.message : 'Не удалось импортировать файл.');
    }
    event.target.value = '';
  }

  if (ledger.error)
    return (
      <main className="loading-screen">
        <h1>Не удалось открыть учет</h1>
        <p>{ledger.error}</p>
        <p>Данные в браузере не изменены. Проверьте доступ к хранилищу и обновите страницу.</p>
      </main>
    );
  if (!ledger.loaded)
    return (
      <main className="loading-screen">
        <div className="brand-mark">▥</div>
        <h1>Ритм</h1>
        <p>Открываем вашу рабочую область...</p>
      </main>
    );

  return (
    <div className="app-shell">
      {mobileNav && (
        <button
          className="nav-backdrop"
          aria-label="Закрыть меню"
          onClick={() => setMobileNav(false)}
        />
      )}
      <aside className={`sidebar ${mobileNav ? 'is-open' : ''}`}>
        <a
          href="#"
          className="brand"
          onClick={(event) => {
            event.preventDefault();
            setPage('overview');
            setMobileNav(false);
          }}
        >
          <span className="brand-mark">
            <i />
            <i />
            <i />
          </span>
          ритм<span className="brand-dot">.</span>
        </a>
        <button
          className="workspace"
          onClick={() => {
            setPage('settings');
            setMobileNav(false);
          }}
        >
          <span className="workspace-avatar">К</span>
          <span>
            <strong>Студия Кедр</strong>
            <small>Рабочее пространство</small>
          </span>
          <ChevronDown size={14} />
        </button>
        <span className="nav-label">РАБОЧАЯ ОБЛАСТЬ</span>
        <nav>
          {navigation.map(({ id, title, Icon }) => (
            <button
              className={page === id ? 'active' : ''}
              key={id}
              onClick={() => {
                setPage(id);
                setMobileNav(false);
              }}
            >
              <Icon size={19} />
              <span>{title}</span>
              {id === 'transactions' && <small>{ledger.entries.length}</small>}
            </button>
          ))}
        </nav>
        <div className="sidebar-bottom">
          <div className="local-status">
            <ShieldCheck size={17} />
            <div>
              Данные под контролем<small>Хранятся на этом устройстве</small>
            </div>
          </div>
          <button
            className="help-link"
            onClick={() => {
              setPage('settings');
              setMobileNav(false);
            }}
          >
            <CircleHelp size={18} />
            Как устроен Ритм
            <ArrowUpRight size={15} />
          </button>
          <div className="profile">
            <span>ЕК</span>
            <div>
              <strong>Егор Кручинин</strong>
              <small>Владелец пространства</small>
            </div>
          </div>
        </div>
      </aside>

      <main className="main-content">
        <header className="topbar">
          <div className="breadcrumb">
            <button
              className="icon-button mobile-menu"
              aria-label="Меню"
              aria-expanded={mobileNav}
              onClick={() => setMobileNav(!mobileNav)}
            >
              <Menu size={20} />
            </button>
            <span>Рабочая область</span>
            <span>/</span>
            <strong>{navigation.find((item) => item.id === page)?.title}</strong>
          </div>
          <span className="local-indicator">
            <i />
            Локальное хранилище
          </span>
        </header>
        <div className="page-heading">
          <div>
            <div className="eyebrow">ФИНАНСЫ СТУДИИ</div>
            <h1>
              {page === 'overview'
                ? 'Все идет в вашем ритме'
                : page === 'transactions'
                  ? 'Каждая операция на месте'
                  : page === 'analytics'
                    ? 'За цифрами видно больше'
                    : 'Ваши данные, ваши правила'}
            </h1>
            <p>
              {page === 'overview'
                ? 'Доходы, расходы и результат бизнеса в одном месте.'
                : page === 'transactions'
                  ? 'Находите платежи, добавляйте записи и держите порядок.'
                  : page === 'analytics'
                    ? 'Смотрите, что приносит доход и куда уходит бюджет.'
                    : 'Сохраняйте копии и переносите операции между устройствами.'}
            </p>
          </div>
          <button className="primary-button" onClick={() => setEditor('new')}>
            <Plus size={18} />
            Добавить операцию
          </button>
        </div>
        <div className="period-toolbar">
          <label className="period-select">
            <CalendarDays size={17} />
            <select
              aria-label="Период"
              value={month}
              onChange={(event) => setMonth(event.target.value)}
            >
              {months.map((item) => (
                <option value={item} key={item}>
                  {monthLabel(item)}
                </option>
              ))}
            </select>
            <ChevronDown size={14} />
          </label>
          <span className="period-caption">Сравнение с предыдущим месяцем</span>
          <button className="text-button export-button" onClick={exportMonth}>
            <Download size={16} />
            Экспорт CSV
          </button>
        </div>

        {page === 'overview' && (
          <OverviewPage
            entries={ledger.entries}
            month={month}
            onTransactions={() => setPage('transactions')}
            onAnalytics={() => setPage('analytics')}
            onEdit={setEditor}
          />
        )}

        {page === 'transactions' && (
          <section className="panel ledger-panel">
            <div className="ledger-toolbar">
              <div className="filter-tabs">
                {[
                  ['all', 'Все'],
                  ['income', 'Доходы'],
                  ['expense', 'Расходы'],
                ].map(([id, label]) => (
                  <button
                    className={type === id ? 'selected' : ''}
                    key={id}
                    onClick={() => setType(id)}
                  >
                    {label}
                  </button>
                ))}
              </div>
              <label className="search-field">
                <Search size={17} />
                <input
                  aria-label="Поиск операций"
                  placeholder="Найти операцию"
                  value={search}
                  onChange={(event) => setSearch(event.target.value)}
                />
              </label>
              <label className="category-filter">
                <ListFilter size={17} />
                <select
                  aria-label="Категория операций"
                  value={category}
                  onChange={(event) => setCategory(event.target.value)}
                >
                  <option value="all">Все категории</option>
                  {categories.map((item) => (
                    <option key={item.id} value={item.id}>
                      {item.label}
                    </option>
                  ))}
                </select>
              </label>
            </div>
            <TransactionTable entries={filtered} onEdit={setEditor} />
            <div className="table-footer">
              <span>Операций: {filtered.length}</span>
              <strong>Итог выборки: {money(totals(filtered).profit)}</strong>
            </div>
          </section>
        )}

        {page === 'analytics' && <AnalyticsPage entries={ledger.entries} month={month} />}

        {page === 'settings' && (
          <DataPage
            entries={ledger.entries}
            onImport={() => importInput.current?.click()}
            notify={notify}
          />
        )}
        <footer className="page-footer">
          <span>Ритм · меньше рутины, больше ясности</span>
          <span>Расчеты в RUB · локальное хранение</span>
        </footer>
      </main>
      <input
        ref={importInput}
        type="file"
        accept=".csv,text/csv"
        hidden
        onChange={(event) => void handleImport(event)}
      />
      {editor && (
        <EntryEditor
          entry={editor === 'new' ? undefined : editor}
          month={month}
          onClose={() => setEditor(null)}
          onSave={(entry) => {
            ledger.save(entry);
            setMonth(entry.date.slice(0, 7));
            notify('Операция сохранена.');
          }}
          onDelete={(id) => {
            ledger.remove(id);
            notify('Операция удалена.');
          }}
        />
      )}
      {notice && (
        <div className="toast" role="status">
          <Check size={19} />
          <span>{notice}</span>
          <button
            className="icon-button"
            aria-label="Закрыть уведомление"
            onClick={() => setNotice('')}
          >
            <X size={16} />
          </button>
        </div>
      )}
    </div>
  );
}
