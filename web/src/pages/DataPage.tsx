import { Download, ShieldCheck, Upload } from 'lucide-react';
import { saveFile } from '../domain/download';
import { exportCsv, type Entry } from '../domain/ledger';

export default function DataPage({
  entries,
  onImport,
  notify,
}: {
  entries: Entry[];
  onImport: () => void;
  notify: (message: string) => void;
}) {
  return (
    <div className="settings-grid">
      <section className="panel settings-card">
        <div className="settings-icon">
          <ShieldCheck />
        </div>
        <h2>Локальное пространство</h2>
        <p>
          Операции сохраняются в этом браузере. Регистрация не нужна, финансовые данные не
          отправляются на сервер. Web и Android хранят свои записи отдельно.
        </p>
        <dl>
          <dt>Пространство</dt>
          <dd>Студия Кедр</dd>
          <dt>Валюта</dt>
          <dd>Российский рубль</dd>
          <dt>Начальный остаток</dt>
          <dd>280 000 ₽</dd>
          <dt>Всего операций</dt>
          <dd>{entries.length}</dd>
        </dl>
      </section>
      <section className="panel settings-card">
        <div className="settings-icon">
          <Download />
        </div>
        <h2>Перенос и резервная копия</h2>
        <p>
          Экспортируйте все операции в CSV. Этот же формат открывается в Excel, аналитическом модуле
          Python и Android-приложении. При импорте существующие ID пропускаются.
        </p>
        <button
          className="primary-button"
          onClick={() => {
            saveFile(exportCsv(entries), 'ritm-all-operations.csv', 'text/csv;charset=utf-8');
            notify('Все операции экспортированы.');
          }}
        >
          <Download size={17} />
          Экспортировать все операции
        </button>
        <button className="secondary-button" onClick={() => onImport()}>
          <Upload size={17} />
          Импортировать CSV
        </button>
        <p className="small-note">
          Очистка данных браузера удалит учет. Регулярно сохраняйте резервную копию.
        </p>
      </section>
    </div>
  );
}
