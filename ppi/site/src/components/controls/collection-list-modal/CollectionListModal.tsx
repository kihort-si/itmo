import './CollectionListModal.scss';
import CardElement from "../card-element/CardElement.tsx";
import { useState } from "react";
import PrimaryButton from "../primary-button/PrimaryButton.tsx";
import ClassicCover from "../../../assets/images/collections/classic.jpg";
import FantasticCover from "../../../assets/images/collections/fantastic.jpg";
import DetectiveCover from "../../../assets/images/collections/detective.jpg";

interface Collection {
  id: string;
  name: string;
  description: string;
  cover: string;
}

interface CollectionListModalProps {
  titleId?: string;
  onCollectionSelected?: (collectionId: string[]) => void;
}

function CollectionListModal({ titleId, onCollectionSelected }: CollectionListModalProps) {
  const [selectedCollections, setSelectedCollections] = useState<string[]>([]);

  const collections: Collection[] = [
    { id: '1', name: "Классика", description: "15 книг", cover: ClassicCover },
    { id: '2', name: "Фантастика", description: "8 книг", cover: FantasticCover },
    { id: '3', name: "Детективы", description: "12 книг", cover: DetectiveCover },
  ];

  const handleCollectionClick = (collectionId: string) => {
    setSelectedCollections(prev => {
      if (prev.includes(collectionId)) {
        return prev.filter(id => id !== collectionId);
      }
      return [...prev, collectionId];
    });
  };

  const handleConfirm = () => {
    if (selectedCollections.length > 0 && onCollectionSelected) {
      onCollectionSelected(selectedCollections);
    }
  };

  return (
    <div
      className="collection-list-modal-container"
      onClick={(e) => e.stopPropagation()}
    >
      <h2 className="collection-list-modal-title" id={titleId}>
        Выберите коллекции
      </h2>
      <div className="collection-list">
        {collections.map((collection) => (
          <div
            key={collection.id}
            onClick={(e) => {
              e.stopPropagation();
              handleCollectionClick(collection.id);
            }}
            className={selectedCollections.includes(collection.id) ? 'selected-collection' : ''}
          >
            <CardElement
              title={collection.name}
              description={collection.description}
              imageUrl={collection.cover}
              button={false}
            />
          </div>
        ))}
      </div>
      <div className="modal-actions">
        <PrimaryButton
          label={`Добавить (${selectedCollections.length})`}
          onClick={handleConfirm}
          type="button"
        />
      </div>
    </div>
  );
}

export default CollectionListModal;