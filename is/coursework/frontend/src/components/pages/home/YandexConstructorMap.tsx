interface YandexConstructorMapProps {
  src: string;
  height: number;
}

export function YandexConstructorMap({ src, height }: YandexConstructorMapProps) {
  const umMatch = src.match(/um=([^&]+)/);
  const um = umMatch ? umMatch[1] : '';

  const iframeSrc = `https://yandex.ru/map-widget/v1/?um=${um}&lang=ru_RU&scroll=true&source=constructor-api`;

  return (
    <iframe
      src={iframeSrc}
      width="100%"
      height={height}
      frameBorder="0"
      allowFullScreen
      style={{ border: 0 }}
      title="Yandex Map"
    />
  );
}
