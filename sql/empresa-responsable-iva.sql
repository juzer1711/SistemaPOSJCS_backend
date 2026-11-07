-- Cambio aditivo para instalaciones existentes. Revisar y aplicar explicitamente.
-- Sin DEFAULT ni backfill: NULL significa pendiente de configurar segun el RUT.
-- No iniciar ventas tributarias nuevas suponiendo que NULL equivale a FALSE.
ALTER TABLE empresa ADD COLUMN IF NOT EXISTS responsable_iva boolean;
