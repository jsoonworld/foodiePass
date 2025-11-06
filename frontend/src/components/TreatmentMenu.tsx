import { memo } from 'react';
import type { MenuItem } from '@/lib/types';
import { ImageIcon } from 'lucide-react';

interface TreatmentMenuProps {
  items: MenuItem[];
}

function TreatmentMenu({ items }: TreatmentMenuProps) {
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
      {items.map((item) => (
        <div
          key={item.id}
          className="bg-card border border-border rounded-xl overflow-hidden shadow-md hover:shadow-lg transition-all"
        >
          <div className="aspect-[4/3] bg-muted relative">
            {item.imageUrl ? (
              <img
                src={item.imageUrl}
                alt={item.translatedName}
                loading="lazy"
                className="w-full h-full object-cover"
              />
            ) : (
              <div className="w-full h-full flex items-center justify-center">
                <ImageIcon className="w-16 h-16 text-muted-foreground opacity-50" />
              </div>
            )}
          </div>
          
          <div className="p-5 bg-gradient-card">
            <h3 className="text-lg font-bold text-foreground mb-1">
              {item.originalName}
            </h3>
            <p className="text-base text-foreground mb-2">
              {item.translatedName}
            </p>
            
            {item.description && (
              <p className="text-sm text-muted-foreground mb-3 line-clamp-3">
                {item.description}
              </p>
            )}
            
            <div className="flex items-baseline gap-2 pt-2 border-t border-border">
              <span className="text-base font-semibold text-primary">
                {item.priceInfo.originalFormatted}
              </span>
              <span className="text-sm text-muted-foreground">
                ({item.priceInfo.convertedFormatted})
              </span>
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}

export default memo(TreatmentMenu);
