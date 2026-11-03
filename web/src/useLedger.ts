import { useEffect, useState } from 'react';
import { validateEntries, type Entry } from './domain/ledger';

const storageKey = 'ritm.entries.v1';

export function useLedger() {
  const [entries, setEntries] = useState<Entry[]>([]);
  const [loaded, setLoaded] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    async function load() {
      try {
        const saved = localStorage.getItem(storageKey);
        const response = saved ? null : await fetch(`${import.meta.env.BASE_URL}seed.json`);
        if (response && !response.ok) throw new Error('Не удалось загрузить начальные данные.');
        const data = validateEntries(saved ? JSON.parse(saved) : await response!.json());
        if (active) {
          setEntries(data);
          setLoaded(true);
        }
      } catch (cause) {
        if (active) setError(cause instanceof Error ? cause.message : 'Ошибка загрузки данных.');
      }
    }
    void load();
    return () => {
      active = false;
    };
  }, []);

  function replace(next: Entry[]) {
    const validated = validateEntries(next);
    localStorage.setItem(storageKey, JSON.stringify(validated));
    setEntries(validated);
  }

  function save(entry: Entry) {
    replace([...entries.filter((item) => item.id !== entry.id), entry]);
  }

  function remove(id: string) {
    replace(entries.filter((entry) => entry.id !== id));
  }

  function merge(incoming: Entry[]) {
    const ids = new Set(entries.map((entry) => entry.id));
    const additions = incoming.filter((entry) => !ids.has(entry.id));
    replace([...entries, ...additions]);
    return additions.length;
  }

  return { entries, loaded, error, save, remove, merge };
}
